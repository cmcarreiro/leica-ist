--RI-100
CREATE OR REPLACE FUNCTION ri_100_proc()
RETURNS TRIGGER AS
$$
DECLARE
    count_consultas INTEGER;
BEGIN
    SELECT COUNT(num_cedula) INTO count_consultas
    FROM consulta 
    WHERE date_part('week', data) = date_part('week', NEW.data)
    AND nome_instituicao = NEW.nome_instituicao;
	
    IF count_consultas < 100 THEN
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'O medico % atingiu o seu limite semanal de 100 consultas na instituicao %.', NEW.num_cedula, NEW.nome_instituicao;
    END IF;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER ri_100
    BEFORE INSERT ON consulta
    FOR EACH ROW
    EXECUTE PROCEDURE ri_100_proc();

--RI-analise
CREATE OR REPLACE FUNCTION ri_analise_proc() RETURNS TRIGGER AS
$$
DECLARE
    medico_especialidade varchar(100);
BEGIN
	SELECT especialidade INTO medico_especialidade
    FROM medico
    WHERE num_cedula=NEW.num_cedula;
	
	IF NEW.num_cedula IS NULL AND NEW.num_doente IS NULL and NEW.data IS NULL THEN--consulta omissa
		RETURN NEW;
	ELSIF NEW.num_cedula IS NOT NULL and NEW.num_doente IS NOT NULL and NEW.data IS NOT NULL THEN--consulta nao omissa
		IF medico_especialidade = NEW.especialidade THEN
			RETURN NEW;
		ELSE
			RAISE EXCEPTION 'A analise e de % mas o medico e especialista em %.', NEW.especialidade, medico_especialidade;
		END IF;
	ELSE
		RAISE EXCEPTION 'A analise com o numero % nao tem os dados corretos.', NEW.num_analise;
	END IF;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER ri_analise
    BEFORE INSERT ON analise
    FOR EACH ROW
    EXECUTE PROCEDURE ri_analise_proc();