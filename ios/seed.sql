-- ============================================================
-- Presencia — Full Test Seed
-- Run this in Supabase SQL Editor (Settings → SQL Editor)
-- Uses your existing group, profile, member, and meeting data.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Create activity_log table (idempotent)
-- ------------------------------------------------------------

create table if not exists activity_log (
  id             uuid        primary key default gen_random_uuid(),
  profile_id     uuid        not null references profile(id) on delete cascade,
  small_group_id uuid        not null references small_group(id) on delete cascade,
  action_type    text        not null,
  description    text        not null,
  created_at     timestamptz not null default now()
);

create index if not exists activity_log_group_idx
  on activity_log(small_group_id, created_at desc);

alter table activity_log enable row level security;

drop policy if exists "Group members can view logs"  on activity_log;
drop policy if exists "Authenticated users can insert logs" on activity_log;

create policy "Group members can view logs"
  on activity_log for select
  using (
    small_group_id in (
      select small_group_id from role_assignment where profile_id = auth.uid()
    )
  );

create policy "Authenticated users can insert logs"
  on activity_log for insert
  with check (profile_id = auth.uid());


-- ------------------------------------------------------------
-- 2. Seed activity_log
-- ------------------------------------------------------------

with ctx as (
  select ra.profile_id, ra.small_group_id
  from   role_assignment ra
  where  ra.small_group_id is not null
  limit  1
),
members as (
  select m.id as member_id,
         m.first_name || ' ' || m.last_name as full_name,
         row_number() over (order by m.created_at) as rn
  from   member m
  join   ctx c on m.small_group_id = c.small_group_id
  where  m.is_active = true and m.is_visitor = false
  limit  5
)
insert into activity_log (profile_id, small_group_id, action_type, description, created_at)

-- 6 meeting submissions (one per week going back)
select c.profile_id, c.small_group_id,
       'meeting_submitted',
       'Reunión del ' || to_char(now() - (n || ' weeks')::interval, 'DD/MM/YYYY'),
       now() - (n || ' weeks')::interval - interval '2 hours'
from ctx c, generate_series(0, 5) as n

union all

-- member archived
select c.profile_id, c.small_group_id,
       'member_archived',
       m.full_name || ' fue archivado',
       now() - interval '3 days'
from ctx c join members m on m.rn = 1

union all

-- member restored
select c.profile_id, c.small_group_id,
       'member_unarchived',
       m.full_name || ' fue restaurado',
       now() - interval '2 days'
from ctx c join members m on m.rn = 1

union all

-- 3 members added
select c.profile_id, c.small_group_id,
       'member_added',
       'Se agregó al miembro ' || m.full_name,
       now() - ((m.rn * 5) || ' days')::interval
from ctx c join members m on m.rn in (2, 3, 4);


-- ------------------------------------------------------------
-- 3. Seed activity_record on the most recent existing meeting
-- ------------------------------------------------------------

with ctx as (
  select ra.profile_id, ra.small_group_id
  from   role_assignment ra
  where  ra.small_group_id is not null
  limit  1
),
target_meeting as (
  select m.id as meeting_id
  from   meeting m
  join   ctx c on m.small_group_id = c.small_group_id
  order  by m.meeting_date desc
  limit  1
),
activity_types as (
  select at.id as type_id,
         at.name,
         at.unit_label,
         row_number() over (order by at.sort_order) as rn
  from   activity_type at
  where  at.is_active = true
  limit  5
)
insert into activity_record (id, meeting_id, activity_type_id, count, notes)
select
  gen_random_uuid(),
  tm.meeting_id,
  at.type_id,
  case at.rn
    when 1 then 3
    when 2 then 7
    when 3 then 12
    when 4 then 2
    when 5 then 5
  end,
  null
from target_meeting tm
cross join activity_types at
on conflict (meeting_id, activity_type_id) do update
  set count = excluded.count;


-- ------------------------------------------------------------
-- 4. Verify
-- ------------------------------------------------------------

select 'activity_log' as tbl, action_type, description,
       to_char(created_at, 'DD/MM/YYYY HH24:MI') as when
from   activity_log
order  by created_at desc
limit  15;

select 'activity_record' as tbl, ar.count,
       at.name as activity, at.unit_label,
       to_char(m.meeting_date, 'DD/MM/YYYY') as meeting_date
from   activity_record ar
join   activity_type at on at.id = ar.activity_type_id
join   meeting m on m.id = ar.meeting_id
order  by at.sort_order
limit  10;
