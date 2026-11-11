-- Avatar do usuario. 'avatar' = id do catalogo (ex.: hero_01_knight) ou 'personalizado' (imagem enviada).
-- NULL = ainda sem avatar (contas antigas); o servidor atribui um padrao UMA vez no primeiro login.
-- 'avatar_imagem' guarda a imagem recortada (data URL JPEG/WebP/PNG, validada e limitada no servidor).
ALTER TABLE usuario ADD COLUMN avatar VARCHAR(40);
ALTER TABLE usuario ADD COLUMN avatar_imagem TEXT;
