 
-- ------------------------------------------------------------ 
--  Criação do usuário e tal...
-- ------------------------------------------------------------ 
--
-- CREATE USER trabalho WITH PASSWORD 'trabalho';
-- 
-- CREATE DATABASE trabalho
--   WITH OWNER = trabalho
--        ENCODING = 'UTF8'
--        LC_COLLATE = 'en_US.utf8'
--        LC_CTYPE = 'en_US.utf8'
--        TEMPLATE = template0;

-- GRANT ALL PRIVILEGES ON DATABASE trabalho TO trabalho;

-- ------------------------------------------------------------ 
--



-- ------------------------------------------------------------ 
-- | Tabela de controle só, valores aqui serão estáticos.
-- ------------------------------------------------------------ 
-- | Serve só pra manter o banco normalizado.
-- ------------------------------------------------------------ 
CREATE TABLE bairros (
    id_bairro SERIAL PRIMARY KEY,
    bairro_name VARCHAR(100) NOT NULL,

    latitude    DECIMAL(10, 2) NOT NULL,
    longitude   DECIMAL(10, 2) NOT NULL
);
-- ------------------------------------------------------------ 
--

--
--
--
--
CREATE TABLE users (
    id_user         SERIAL      PRIMARY KEY,
    user_name       VARCHAR(100)        NOT NULL,

    email           VARCHAR(80) UNIQUE  NOT NULL,

    password_hash   VARCHAR(80)         NOT NULL,
    cpf             VARCHAR(64) UNIQUE  NOT NULL, -- HASHED

    id_bairro       INT,
    
    type            CHAR(2)             NOT NULL,

    created_at      TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT FK_BAIRRO FOREIGN KEY (id_bairro) REFERENCES bairros(id_bairro),
    CONSTRAINT CK_TYPE CHECK  ( type IN ( 'PR', 'CI' ) ) 
);

--
--
--
--
CREATE TABLE posts (
    id_post SERIAL PRIMARY KEY,
    title VARCHAR(60) NOT NULL,
    body TEXT NOT NULL,

    id_bairro INT, 
    id_user INT NOT NULL,
        
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_BAIRRO FOREIGN KEY (id_bairro) REFERENCES bairros(id_bairro),
    CONSTRAINT FK_USER FOREIGN KEY (id_user) REFERENCES users(id_user)
);

--
--
--
--
CREATE TABLE post_attachments (
    id_attachment SERIAL PRIMARY KEY,
    id_post INT NOT NULL,
    attachment_name VARCHAR(80) NOT NULL,
    path VARCHAR(80) NOT NULL,
    size_bytes INT NOT NULL,

    CONSTRAINT FK_POST FOREIGN KEY (id_post) REFERENCES posts(id_post)
);

--
--
--
CREATE TABLE post_interactions (

    id_interaction SERIAL PRIMARY KEY,

    id_post INT NOT NULL,
    id_user INT NOT NULL,

    interaction VARCHAR(20) NOT NULL,

    CONSTRAINT FK_POST FOREIGN KEY (id_post) REFERENCES posts(id_post),
    CONSTRAINT FK_USER FOREIGN KEY (id_user) REFERENCES users(id_user)
);

--
--
--
CREATE TABLE post_comments (

    id_comment SERIAL PRIMARY KEY,

    id_post INT NOT NULL,
    id_user INT NOT NULL,
    
    comment TEXT NOT NULL,

    CONSTRAINT FK_POST FOREIGN KEY (id_post) REFERENCES posts(id_post),
    CONSTRAINT FK_USER FOREIGN KEY (id_user) REFERENCES users(id_user)
);






-- ------------------------------------------------------------
--  /!\ Required Insertions
-- ------------------------------------------------------------

INSERT INTO bairros 
    (bairro_name, latitude, longitude )
VALUES 
    ( 'Alvorada', 0.0, 0.0 ),
    ( 'Centro', 0.0, 0.0 ),
    ( 'Nova Parobé', 0.0, 0.0 ),
    ( 'Guarani', 0.0, 0.0 ),
    ( 'Guarujá', 0.0, 0.0 ),
    ( 'Santa Cristina do Pinhal', 0.0, 0.0 )
    ;


-- ---- [!] ---------------------------------------------------
--  |   TEM QUE PREPARAR A INSERÇÃO DA PREFEITURA
-- ------------------------------------------------------------