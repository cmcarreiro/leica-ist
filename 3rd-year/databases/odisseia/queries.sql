-- 1. Qual o concelho onde se fez o maior volume de vendas hoje?
WITH
	volume(num_regiao, num_concelho, sum) AS (
		SELECT num_regiao, num_concelho, SUM(quant * preco)
		FROM venda_farmacia vf JOIN instituicao i ON vf.inst = i.nome
		WHERE data_registo = CURRENT_DATE
		GROUP BY num_regiao, num_concelho),

	maxVolume(max) AS (
		SELECT MAX(sum)
		FROM volume)

SELECT num_regiao, num_concelho
FROM maxVolume, volume
WHERE volume.sum = maxVolume.max;



-- 2. Qual o medico que mais prescreveu no 1o semestre de 2019 em cada regiao?
WITH 
	numPrescricao(num_cedula, num_regiao, count) AS(
		SELECT num_cedula, num_regiao, COUNT(num_cedula)
		FROM prescricao NATURAL JOIN consulta JOIN instituicao ON nome = nome_instituicao
		WHERE data BETWEEN '01-01-2019' AND '30-06-2019'
		GROUP BY num_cedula, num_regiao),

	maxPrescricao(num_regiao, max) AS(
		SELECT num_regiao, MAX(count)
		FROM numPrescricao
		GROUP BY num_regiao)

SELECT num_cedula, maxPrescricao.num_regiao
FROM numPrescricao NATURAL JOIN maxPrescricao
WHERE count = max;



-- 3. Quais são os médicos que já prescreveram aspirina em receitas aviadas em todas as farmácias do concelho de Arouca este ano?
WITH 
	numFarmacia(num_cedula, num) AS (
		SELECT num_cedula, COUNT(distinct inst)
		FROM prescricao_venda NATURAL JOIN venda_farmacia JOIN instituicao ON inst = instituicao.nome
		WHERE substancia = 'aspirina'
		AND date_part('year', data_registo) = date_part('year', CURRENT_DATE)
		AND num_regiao=0 AND num_concelho=0
		GROUP BY num_cedula),
		
	totalFarmacias(total) AS(
		SELECT COUNT(*)
		FROM instituicao
		WHERE num_regiao=0 AND num_concelho=0 AND tipo = 'farmacia') --Arouca

SELECT num_cedula
FROM numFarmacia, totalFarmacias
WHERE num = total;



-- 4. Quais são os doentes que já fizeram análises mas ainda não aviaram prescrições este mês?
(SELECT num_doente
FROM analise
WHERE num_doente IS NOT NULL
AND date_part('month', data_registo)=date_part('month', CURRENT_DATE))
EXCEPT
(SELECT num_doente
FROM prescricao_venda
WHERE date_part('month', data)=date_part('month', CURRENT_DATE))
	