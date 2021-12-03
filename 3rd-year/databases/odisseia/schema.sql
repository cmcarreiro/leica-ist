DROP TABLE IF EXISTS instituicao CASCADE;

CREATE TABLE instituicao (
    nome varchar(100) PRIMARY KEY,
    tipo varchar(100) NOT NULL CHECK (tipo IN ('farmacia', 'laboratorio', 'clinica', 'hospital')),
    num_regiao integer NOT NULL,
    num_concelho integer NOT NULL
    -- FOREIGN KEY (num_regiao, num_concelho) REFERENCES concelho (num_regiao, num_concelho) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS medico CASCADE;

CREATE TABLE medico (
    num_cedula integer PRIMARY KEY,
    nome varchar(100) NOT NULL,
    especialidade varchar(100) NOT NULL
);

DROP TABLE IF EXISTS consulta CASCADE;

CREATE TABLE consulta (
    num_cedula integer REFERENCES medico (num_cedula) ON DELETE CASCADE ON UPDATE CASCADE,
    num_doente integer,
    data date CHECK (extract(DOW FROM data) NOT IN (6, 0)), -- dom:0, seg:1, ter:2, ...
    nome_instituicao varchar(100) NOT NULL REFERENCES instituicao (nome) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (num_cedula, num_doente, data),
    UNIQUE (num_doente, data, nome_instituicao)
);

DROP TABLE IF EXISTS prescricao CASCADE;

CREATE TABLE prescricao (
    num_cedula integer,
    num_doente integer,
    data date,
    substancia varchar(100),
    quant integer NOT NULL,
    PRIMARY KEY (num_cedula, num_doente, data, substancia),
    FOREIGN KEY (num_cedula, num_doente, data) REFERENCES consulta (num_cedula, num_doente, data) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS analise CASCADE;

CREATE TABLE analise (
    num_analise integer PRIMARY KEY,
    especialidade varchar(100) NOT NULL,
    num_cedula integer,
    num_doente integer,
    data date,
    data_registo date NOT NULL,
    nome varchar(100) NOT NULL,
    quant integer NOT NULL,
    inst varchar(100) NOT NULL REFERENCES instituicao (nome) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (num_cedula, num_doente, data) REFERENCES consulta (num_cedula, num_doente, data) ON DELETE SET NULL ON UPDATE CASCADE
);

DROP TABLE IF EXISTS venda_farmacia CASCADE;

CREATE TABLE venda_farmacia (
    num_venda integer PRIMARY KEY,
    data_registo date NOT NULL,
    substancia varchar(100) NOT NULL,
    quant integer NOT NULL,
    preco money NOT NULL,
    inst varchar(100) NOT NULL REFERENCES instituicao (nome) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS prescricao_venda CASCADE;

CREATE TABLE prescricao_venda (
    num_cedula integer,
    num_doente integer,
    data date,
    substancia varchar(100),
    num_venda integer NOT NULL REFERENCES venda_farmacia (num_venda) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (num_cedula, num_doente, data, substancia, num_venda),
    FOREIGN KEY (num_cedula, num_doente, data, substancia) REFERENCES prescricao (num_cedula, num_doente, data, substancia) ON DELETE CASCADE ON UPDATE CASCADE
);
