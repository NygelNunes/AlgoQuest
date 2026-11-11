-- Papeis de usuario, tema de ilustracao do modulo e recurso visual da questao.
-- papel: USUARIO (padrao) | ADMIN | MASTER. Contas existentes ficam USUARIO; o Master e definido por ALGOQUEST_USUARIO_INICIAL.
ALTER TABLE usuario ADD COLUMN papel VARCHAR(10) NOT NULL DEFAULT 'USUARIO';
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_papel CHECK (papel IN ('USUARIO', 'ADMIN', 'MASTER'));
-- tema: chave da ilustracao SVG do frontend (arvore, busca, ordenacao, hash, analise, padrao). NULL = deduzida do titulo (modulos antigos).
ALTER TABLE modulo ADD COLUMN tema VARCHAR(20);
-- visual: imagem (data URL png/jpeg/webp) ou SVG inline validado; NULL = sem recurso visual.
ALTER TABLE exercicio ADD COLUMN visual TEXT;
