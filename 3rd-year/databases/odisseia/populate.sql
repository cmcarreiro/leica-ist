INSERT INTO instituicao
    VALUES ('Hospital da Misericordia de Arouca', 'hospital', 0, 0);

INSERT INTO instituicao
    VALUES ('Farmacia F. C. Arouca', 'farmacia', 0, 0);

INSERT INTO instituicao
    VALUES ('Farmacia Penso', 'farmacia', 0, 0);

INSERT INTO instituicao
    VALUES ('Farmacia Lopes', 'farmacia', 1, 1);

INSERT INTO instituicao
    VALUES ('Centro Hospitalar de Sao Francisco', 'hospital', 1, 2);

INSERT INTO instituicao
    VALUES ('MiMed Telheiras', 'clinica', 2, 3);

INSERT INTO instituicao
    VALUES ('Lab 15', 'laboratorio', 2, 3);

--INSERT INTO instituicao
--    VALUES ('Erro', 'erro', 2, 3); -- erro R1-instituicao-1



INSERT INTO medico
    VALUES (0, 'Dra. Pipoca', 'Psiquiatria');

INSERT INTO medico
    VALUES (1, 'Dr. Olliver', 'Nutricao');

INSERT INTO medico
    VALUES (2, 'Dra. Popica', 'Medicina Nuclear');

INSERT INTO medico
    VALUES (3, 'Dr. Cris', 'Dermatologia');

INSERT INTO medico
    VALUES (4, 'Drx. Cata', 'Otorrinolaringologia');

INSERT INTO medico
    VALUES (5, 'Dra. Monica', 'Tetanologia');



INSERT INTO consulta
    VALUES (0, 0, '2019-02-15', 'Farmacia F. C. Arouca');

INSERT INTO consulta
    VALUES (0, 1, '2019-02-18', 'Farmacia Penso');

INSERT INTO consulta
    VALUES (3, 2, '2019-01-03', 'Lab 15');

INSERT INTO consulta
    VALUES (5, 3, '2019-01-18', 'MiMed Telheiras');

INSERT INTO consulta
    VALUES (0, 4, '2020-02-17', 'Farmacia F. C. Arouca');

INSERT INTO consulta
    VALUES (0, 5, '2020-02-18', 'Farmacia Penso');

INSERT INTO consulta
    VALUES (4, 6, '2020-11-02', 'Centro Hospitalar de Sao Francisco');

--INSERT INTO consulta
--    VALUES (4, 6, '2020-11-01', 'Centro Hospitalar de Sao Francisco'); -- erro R1-consulta-1

--INSERT INTO consulta
--    VALUES (1, 6, '2020-11-02', 'Centro Hospitalar de Sao Francisco'); -- erro R1-consulta-2



INSERT INTO prescricao
    VALUES (5, 3, '2019-01-18', 'wd-40', 3);

INSERT INTO prescricao
    VALUES (0, 0, '2019-02-15', 'diclofenac sodico', 123);

INSERT INTO prescricao
    VALUES (0, 1, '2019-02-18', 'pate do bom', 321);

INSERT INTO prescricao
    VALUES (0, 4, '2020-02-17', 'aspirina', 456);

INSERT INTO prescricao
    VALUES (0, 5, '2020-02-18', 'aspirina', 654);



INSERT INTO analise
    VALUES (0, 'Otorrinolaringologia', 4, 6, '2020-11-02', '2020-11-02', 'numero orelhas', 5, 'Centro Hospitalar de Sao Francisco');

INSERT INTO analise
    VALUES (1, 'Neurologia', NULL, NULL, NULL, '2020-11-03', 'numero neuronios', 3, 'MiMed Telheiras');



INSERT INTO venda_farmacia
    VALUES (0, '2020-02-15', 'aspirina', 456, 12.34, 'Farmacia F. C. Arouca');

INSERT INTO venda_farmacia
    VALUES (1, '2020-02-18', 'aspirina', 654, 43.21, 'Farmacia Penso');

INSERT INTO venda_farmacia
    VALUES (2, CURRENT_DATE, 'brufen', 666, 11.99, 'Farmacia Lopes');

INSERT INTO venda_farmacia
    VALUES (3, CURRENT_DATE, 'brufenon', 123, 0.99, 'Farmacia Penso');



INSERT INTO prescricao_venda
    VALUES (0, 4, '2020-02-17', 'aspirina', 0);

INSERT INTO prescricao_venda
    VALUES (0, 5, '2020-02-18', 'aspirina', 1);