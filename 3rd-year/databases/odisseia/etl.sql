INSERT INTO d_instituicao
SELECT nextval('d_instituicao_id_inst_seq'), instituicao.* FROM instituicao;

INSERT INTO d_tempo
SELECT nextval('d_tempo_id_tempo_seq'), date_part('day', data_registo), date_part('dow', data_registo), date_part('week', data_registo), date_part('month', data_registo), (date_part('month', data_registo)-1)/3 + 1,date_part('year', data_registo)
FROM prescricao_venda NATURAL JOIN venda_farmacia;

INSERT INTO d_tempo
SELECT nextval('d_tempo_id_tempo_seq'), date_part('day', data_registo), date_part('dow', data_registo), date_part('week', data_registo), date_part('month', data_registo), (date_part('month', data_registo)-1)/3 + 1,date_part('year', data_registo)
FROM analise;

INSERT INTO f_presc_venda
SELECT num_venda, num_cedula, num_doente, id_tempo, id_inst, substancia, quant
FROM prescricao_venda NATURAL JOIN venda_farmacia JOIN d_instituicao ON venda_farmacia.inst = d_instituicao.nome, d_tempo
WHERE date_part('day', data_registo) = d_tempo.dia AND date_part('month', data_registo) = d_tempo.mes AND date_part('year', data_registo)=d_tempo.ano;

INSERT INTO f_analise
SELECT num_analise, num_cedula, num_doente, id_tempo, Id_inst, analise.nome, quant
FROM analise JOIN d_instituicao ON inst = d_instituicao.nome, d_tempo
WHERE date_part('day', data_registo) = d_tempo.dia AND date_part('month', data_registo) = d_tempo.mes AND date_part('year', data_registo)=d_tempo.ano;
