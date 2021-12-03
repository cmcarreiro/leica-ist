DROP TABLE IF EXISTS d_tempo CASCADE;
CREATE TABLE d_tempo(
    id_tempo SERIAL PRIMARY KEY, --supostamente incrementa id automaticamente
    dia INTEGER NOT NULL,
    dia_da_semana INTEGER NOT NULL,
    semana INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    trimestre INTEGER NOT NULL,
    ano INTEGER NOT NULL
);

DROP TABLE IF EXISTS d_instituicao CASCADE;
CREATE TABLE d_instituicao(
    id_inst SERIAL PRIMARY KEY,
    nome CHAR(100) NOT NULL REFERENCES instituicao(nome) ON DELETE CASCADE ON UPDATE CASCADE,
    tipo CHAR(100) NOT NULL,
    num_regiao INTEGER NOT NULL, --REFERENCES regiao(num_regiao) ON DELETE CASCADE ON UPDATE CASCADE,
    num_concelho INTEGER NOT NULL --REFERENCES concelho(num_concelho) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS f_presc_venda CASCADE;
CREATE TABLE f_presc_venda(
    id_presc_venda INTEGER PRIMARY KEY REFERENCES prescricao_venda(num_venda) ON DELETE CASCADE ON UPDATE CASCADE,
    id_medico INTEGER NOT NULL REFERENCES medico(num_cedula) ON DELETE CASCADE ON UPDATE CASCADE,
    num_doente INTEGER NOT NULL,
    id_data_registo INTEGER NOT NULL REFERENCES d_tempo(id_tempo) ON DELETE CASCADE ON UPDATE CASCADE,
    id_inst INTEGER NOT NULL REFERENCES d_instituicao(id_inst) ON DELETE CASCADE ON UPDATE CASCADE,
    substancia CHAR(100) NOT NULL,
    quant INTEGER NOT NULL
);

DROP TABLE IF EXISTS f_analise CASCADE;
CREATE TABLE f_analise(
    id_analise INTEGER PRIMARY KEY REFERENCES analise(num_analise) ON DELETE CASCADE ON UPDATE CASCADE,
    id_medico INTEGER REFERENCES medico(num_cedula) ON DELETE SET NULL ON UPDATE CASCADE,
    num_doente INTEGER,
    id_data_registo INTEGER NOT NULL REFERENCES d_tempo(id_tempo) ON DELETE CASCADE ON UPDATE CASCADE,
    id_inst INTEGER NOT NULL REFERENCES d_instituicao(id_inst) ON DELETE CASCADE ON UPDATE CASCADE,
    nome CHAR(100) NOT NULL,
    quant INTEGER NOT NULL
);