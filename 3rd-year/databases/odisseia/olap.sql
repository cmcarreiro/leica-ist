SELECT especialidade, ano, mes, COUNT(id_analise)
FROM f_analise JOIN medico ON id_medico=num_cedula JOIN d_tempo ON id_data_registo=id_tempo
WHERE f_analise.nome = 'glicemia' AND ano BETWEEN 2017 AND 2020
GROUP BY GROUPING SETS(especialidade, ano, mes);


WITH total_presc_venda(substancia, num_concelho, mes, dia_da_semana, quant_total, n_prescricoes)AS(
    SELECT substancia, num_concelho, mes,dia_da_semana, SUM(quant), COUNT(id_presc_venda)
    FROM f_presc_venda JOIN d_tempo ON id_data_registo=id_tempo NATURAL JOIN d_instituicao
    WHERE trimestre=1 AND ano=2020 AND num_regiao=0 --assumimos que Lisboa tem num_regiao=0
    GROUP BY substancia, num_concelho, mes, dia_da_semana
)
SELECT substancia, num_concelho, mes, dia_da_semana, SUM(quant_total), SUM(n_prescricoes)/4 AS n_medio_prescricoes_diario
FROM total_presc_venda
GROUP BY substancia, num_concelho, mes, dia_da_semana
UNION
SELECT substancia, num_concelho, mes, NULL, SUM(quant_total), SUM(n_prescricoes)/30 AS n_medio_prescricoes_diario
FROM total_presc_venda
GROUP BY substancia, num_concelho, mes
UNION
SELECT substancia, num_concelho, NULL, NULL, SUM(quant_total), SUM(n_prescricoes)/90 AS n_medio_prescricoes_diario
FROM total_presc_venda
GROUP BY substancia, num_concelho
UNION
SELECT substancia, NULL, NULL, NULL, SUM(quant_total), SUM(n_prescricoes)/90 AS n_medio_prescricoes_diario
FROM total_presc_venda
GROUP BY substancia;