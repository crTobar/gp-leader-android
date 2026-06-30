-- Ejecutar en Supabase SQL Editor para reparar gp_login y gp_set_password.
-- Sin esto, las funciones que usan crypt() fallan con:
--   function crypt(text, text) does not exist

CREATE EXTENSION IF NOT EXISTS pgcrypto;
