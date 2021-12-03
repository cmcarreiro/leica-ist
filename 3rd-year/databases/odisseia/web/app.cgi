#!/usr/bin/python3

from wsgiref.handlers import CGIHandler
from flask import Flask
from flask import render_template, request, redirect, url_for

## Libs postgres
import psycopg2
import psycopg2.extras

app = Flask(__name__)

## SGBD configs
DB_HOST="db.tecnico.ulisboa.pt"
DB_USER="" 
DB_DATABASE=DB_USER
DB_PASSWORD=""
DB_CONNECTION_STRING = "host=%s dbname=%s user=%s password=%s" % (DB_HOST, DB_DATABASE, DB_USER, DB_PASSWORD)


## Runs the function once the root page is requested.
## The request comes with the folder structure setting ~/web as the root
@app.route('/')
def index():
  return render_template("index.html")


@app.route('/instituicao')
def list_instituicao():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT * FROM instituicao;"
    cursor.execute(query)
    return render_template("instituicao.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/instituicao_apagar')
def instituicao_apagar():
  try:
    return render_template("instituicao_apagar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/instituicao_apagar_ok', methods=["POST"])
def instituicao_apagar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''DELETE FROM instituicao WHERE nome = %s;'''
    data = (request.form["nome"],)
    cursor.execute(query, data)
    return redirect(url_for('list_instituicao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/instituicao_editar')
def instituicao_editar():
  try:
    return render_template("instituicao_editar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/instituicao_editar_ok', methods=["POST"])
def instituicao_editar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''UPDATE instituicao SET tipo=%s, num_regiao=%s, num_concelho=%s WHERE nome = %s;'''
    data = (request.form["tipo"], request.form["num_regiao"], request.form["num_concelho"], request.form["nome"],)
    cursor.execute(query, data)
    return redirect(url_for('list_instituicao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/instituicao_inserir')
def instituicao_inserir():
  try:
    return render_template("instituicao_inserir.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/instituicao_inserir_ok', methods=["POST"])
def instituicao_inserir_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO instituicao VALUES (%s, %s, %s, %s);'''
    data = (request.form["nome"], request.form["tipo"], request.form["num_regiao"], request.form["num_concelho"])
    cursor.execute(query, data)
    return redirect(url_for('list_instituicao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()



@app.route('/medico')
def list_medico():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT * FROM medico;"
    cursor.execute(query)
    return render_template("medico.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/medico_apagar')
def medico_apagar():
  try:
    return render_template("medico_apagar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/medico_apagar_ok', methods=["POST"])
def medico_apagar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''DELETE FROM medico WHERE num_cedula = %s;'''
    data = (request.form["num_cedula"])
    cursor.execute(query, data)
    return redirect(url_for('list_medico'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/medico_editar')
def medico_editar():
  try:
    return render_template("medico_editar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/medico_editar_ok', methods=["POST"])
def medico_editar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''UPDATE medico SET nome=%s, especialidade=%s WHERE num_cedula = %s;'''
    data = (request.form["nome"], request.form["especialidade"], request.form["num_cedula"])
    cursor.execute(query, data)
    return redirect(url_for('list_medico'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/medico_inserir')
def medico_inserir():
  try:
    return render_template("medico_inserir.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/medico_inserir_ok', methods=["POST"])
def medico_inserir_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO medico VALUES(%s, %s, %s);'''
    data = (request.form["num_cedula"], request.form["nome"], request.form["especialidade"])
    cursor.execute(query, data)
    return redirect(url_for('list_medico'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/analise')
def list_analise():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT * FROM analise;"
    cursor.execute(query)
    return render_template("analise.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/analise_apagar')
def analise_apagar():
  try:
    return render_template("analise_apagar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/analise_apagar_ok', methods=["POST"])
def analise_apagar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''DELETE FROM analise WHERE num_analise = %s;'''
    data = (request.form["num_analise"])
    cursor.execute(query, data)
    return redirect(url_for('list_analise'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/analise_editar')
def analise_editar():
  try:
    return render_template("analise_editar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/analise_editar_ok', methods=["POST"])
def analise_editar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''UPDATE analise SET especialidade=%s, num_cedula=%s, num_doente = %s,  data = %s, data_registo = %s, nome =%s, quant = %s, inst = %s WHERE num_analise = %s;'''
    data = (request.form["especialidade"], request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["data_registo"], request.form["nome"], request.form["quant"], request.form["inst"], request.form["num_analise"])
    cursor.execute(query, data)
    return redirect(url_for('list_analise'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/analise_inserir')
def analise_inserir():
  try:
    return render_template("analise_inserir.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/analise_inserir_ok', methods=["POST"])
def analise_inserir_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO analise VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s);'''
    data = (request.form["num_analise"], request.form["especialidade"], request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["data_registo"], request.form["nome"], request.form["quant"], request.form["inst"])
    cursor.execute(query, data)
    return redirect(url_for('list_analise'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()


@app.route('/prescricao')
def list_prescricao():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT * FROM prescricao;"
    cursor.execute(query)
    return render_template("prescricao.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/prescricao_apagar')
def prescricao_apagar():
  try:
    return render_template("prescricao_apagar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/prescricao_apagar_ok', methods=["POST"])
def prescricao_apagar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''DELETE FROM prescricao WHERE num_cedula = %s AND num_doente = %s AND data = %s AND substancia = %s;'''
    data = (request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["substancia"])
    cursor.execute(query, data)
    return redirect(url_for('list_prescricao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/prescricao_editar')
def prescricao_editar():
  try:
    return render_template("prescricao_editar.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/prescricao_editar_ok', methods=["POST"])
def prescricao_editar_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    # Esta versão é vuneravel a SQL injection
    query = f'''UPDATE prescricao SET quant=%s WHERE num_cedula = %s AND num_doente = %s AND data = %s AND substancia = %s;'''
    data = (request.form["quant"], request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["substancia"])
    cursor.execute(query, data)
    return redirect(url_for('list_prescricao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/prescricao_inserir')
def prescricao_inserir():
  try:
    return render_template("prescricao_inserir.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/prescricao_inserir_ok', methods=["POST"])
def prescricao_inserir_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO prescricao VALUES(%s, %s, %s, %s, %s);'''
    data = (request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["substancia"], request.form["quant"])
    cursor.execute(query, data)
    return redirect(url_for('list_prescricao'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/venda_sem_prescricao')
def venda_sem_prescricao():
  try:
    return render_template("venda_sem_prescricao.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/venda_sem_prescricao_ok', methods=["POST"])
def venda_sem_prescricao_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO venda_farmacia VALUES(%s, %s, %s, %s, %s, %s);'''
    data = (request.form["num_venda"], request.form["data_registo"], request.form["substancia"], request.form["quant"], request.form["preco"], request.form["inst"]) 
    cursor.execute(query, data)
    return redirect(url_for('index'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()

@app.route('/selecionar_prescricao')
def selecionar_prescricao():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT * FROM prescricao;"
    cursor.execute(query)
    return render_template("selecionar_prescricao.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/venda_com_prescricao')
def venda_com_prescricao():
  try:
    return render_template("venda_com_prescricao.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/venda_com_prescricao_ok', methods=["POST"])
def venda_com_prescricao_ok():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = f'''INSERT INTO venda_farmacia VALUES(%s, %s, %s, %s, %s, %s); INSERT INTO prescricao_venda VALUES (%s, %s, %s, %s, %s);'''
    data = (request.form["num_venda"], request.form["data_registo"], request.form["substancia"], request.form["quant"], request.form["preco"], request.form["inst"], request.form["num_cedula"], request.form["num_doente"], request.form["data"], request.form["substancia"], request.form["num_venda"],) 
    
    cursor.execute(query, data)

    return redirect(url_for('index'))
  except Exception as e:
    return str(e) 
  finally:
    dbConn.commit()
    cursor.close()
    dbConn.close()


@app.route('/listar_substancia_form')
def listar_substancia_form():
  try:
    return render_template("listar_substancia_form.html", params=request.args)
  except Exception as e:
    return str(e)

@app.route('/listar_substancia', methods =["POST"])
def listar_substancia():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = "SELECT substancia FROM prescricao WHERE num_cedula=%s AND date_part('month', data)=%s AND date_part('year', data)=%s;"
    data=(request.form["num_cedula"], request.form["mes"], request.form["ano"])
    cursor.execute(query, data)
    return render_template("listar_substancia.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()

@app.route('/listar_glicemia')
def listar_glicemia():
  dbConn=None
  cursor=None
  try:
    dbConn = psycopg2.connect(DB_CONNECTION_STRING)
    cursor = dbConn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    query = """WITH
    glicemia(num_doente, quant, num_regiao, num_concelho) AS (
        SELECT num_doente, quant, num_regiao, num_concelho
        FROM analise JOIN instituicao ON analise.inst = instituicao.nome
        WHERE analise.nome = 'glicemia'),

    maxPorConcelho(max, num_regiao, num_concelho) AS (
        SELECT MAX(quant), num_regiao, num_concelho
        FROM glicemia
        GROUP BY num_regiao, num_concelho),
    
    minPorConcelho(min, num_regiao, num_concelho) AS (
        SELECT MIN(quant), num_regiao, num_concelho
        FROM glicemia
        GROUP BY num_regiao, num_concelho)

SELECT num_doente, quant, num_regiao, num_concelho
FROM glicemia NATURAL JOIN maxPorConcelho NATURAL JOIN minPorConcelho
WHERE glicemia.quant=maxPorConcelho.max OR glicemia.quant=minPorConcelho.min;"""
    cursor.execute(query)
    return render_template("listar_glicemia.html", cursor=cursor)
  except Exception as e:
    return str(e) #Renders a page with the error.
  finally:
    cursor.close()
    dbConn.close()


CGIHandler().run(app)
