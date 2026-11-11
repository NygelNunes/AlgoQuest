import subprocess, time, sys, json, os, base64, io
from playwright.sync_api import sync_playwright
from PIL import Image
os.makedirs("/tmp/shots", exist_ok=True)
srv = subprocess.Popen([sys.executable, "dev-tools/mock-api/mock-api.py"]); time.sleep(1.2)
BASE = "http://127.0.0.1:5500/"; R = []
def ck(n, c, x=""): R.append(bool(c)); print(("PASS " if c else "FAIL ") + n + (" -> " + str(x)[:110] if x else ""))
def api(pg, metodo, caminho, corpo=None, token=None):
    return pg.evaluate("""async ([m,p,b,t])=>{const h={'Content-Type':'application/json'};if(t)h.Authorization='Bearer '+t;
      const r=await fetch('http://127.0.0.1:8080/api'+p,{method:m,headers:h,body:b?JSON.stringify(b):undefined});let j=null;try{j=await r.json()}catch(e){};return [r.status,j]}""", [metodo, caminho, corpo, token])
def login(pg, nome, senha):
    s, j = api(pg, "POST", "/auth/login", {"nomeUsuario": nome, "senha": senha}); return j["token"]
def registrar(pg, nome):
    s, j = api(pg, "POST", "/auth/registrar", {"nomeUsuario": nome, "senha": "senha-forte-1"}); return j["token"], j["id"]
def entrar_ui(ctx, nome, senha="senha-forte-1"):
    pg = ctx.new_page(); pg.goto(BASE + "index.html"); pg.fill("#usuario", nome); pg.fill("#senha", senha); pg.click("#btnEntrar"); pg.wait_for_url("**/modulos.html"); pg.wait_for_selector("#retrato"); return pg
def aba(pg, k): pg.click(f".adm__aba[data-aba={k}]"); pg.wait_for_timeout(250)
def svg_ok(): return '<svg viewBox="0 0 100 50"><circle cx="25" cy="25" r="12" fill="none" stroke="#dcc084" stroke-width="3"/><path d="M40 25H90" stroke="#dcc084" stroke-width="3"/></svg>'
try:
    with sync_playwright() as p:
        b = p.chromium.launch(); W = {"width": 1366, "height": 850}
        base = b.new_context(viewport=W).new_page(); base.goto(BASE + "index.html"); erros = []
        tm = login(base, "mestre", "senha-master-1"); tc, idc = registrar(base, "carlos"); tb, idb = registrar(base, "bia")
        ctxc = b.new_context(viewport=W); pc = entrar_ui(ctxc, "carlos")
        ck("comum: sem item Administração no menu", pc.locator('.menu__item[href="admin.html"]').count() == 0)
        pc.goto(BASE + "admin.html"); pc.wait_for_url("**/modulos.html", timeout=5000); ck("comum: admin.html redireciona para a Central", True)
        ck("comum: GET /admin/modulos -> 403", api(base, "GET", "/admin/modulos", None, tc)[0] == 403)
        ck("comum: POST /admin/modulos -> 403", api(base, "POST", "/admin/modulos", {"titulo": "X", "descricao": "Y"}, tc)[0] == 403)
        ck("comum: GET /admin/usuarios -> 403", api(base, "GET", "/admin/usuarios", None, tc)[0] == 403)
        ck("comum: PUT papel -> 403", api(base, "PUT", f"/admin/usuarios/{idc}/papel", {"papel": "ADMIN"}, tc)[0] == 403)
        ck("sem token -> 401", api(base, "GET", "/admin/modulos")[0] == 401)
        ck("comum: continua usando o app (lista de modulos)", api(base, "GET", "/modulos", None, tc)[0] == 200)
        cm = b.new_context(viewport=W); pm = entrar_ui(cm, "mestre", "senha-master-1")
        pm.click('.menu__item[href="admin.html"]'); pm.wait_for_selector(".adm__item")
        ck("master: 4 abas (inclui Usuários)", pm.locator(".adm__aba").all_inner_texts() == ["Módulos", "Questões", "Importar", "Usuários"], pm.locator(".adm__aba").all_inner_texts())
        aba(pm, "usuarios"); linhas = pm.locator(".adm__lin:not(.adm__lin--cab)")
        ck("master: lista todos os usuários", linhas.count() >= 3, linhas.count())
        ck("master: conta Master protegida (sem botão)", pm.locator('.adm__lin:has-text("mestre") button').count() == 0)
        btn = pm.locator('.adm__lin:has-text("bia") button'); btn.click(); pm.wait_for_selector("dialog.adm__dlg[open]")
        pm.click("dialog [data-n]")
        papel_bia = lambda: [u for u in api(base, "GET", "/admin/usuarios", None, tm)[1] if u["nomeUsuario"] == "bia"][0]["papel"]
        ck("master: cancelar na confirmação não altera (bia segue USUARIO)", papel_bia() == "USUARIO")
        pm.locator('.adm__lin:has-text("bia") button').click(); pm.click("dialog [data-s]"); pm.wait_for_timeout(400)
        ck("master: promove bia a ADMIN (UI + banco)", papel_bia() == "ADMIN"); pm.screenshot(path="/tmp/shots/adm_usuarios.png")
        ck("master: PUT papel de si mesmo -> 403", api(base, "PUT", "/admin/usuarios/1/papel", {"papel": "USUARIO"}, tm)[0] == 403)
        ck("master: promover alguém a MASTER -> 403", api(base, "PUT", f"/admin/usuarios/{idc}/papel", {"papel": "MASTER"}, tm)[0] == 403)
        ck("master: papel inválido -> 400", api(base, "PUT", f"/admin/usuarios/{idc}/papel", {"papel": "REI"}, tm)[0] == 400)
        ck("master: usuário inexistente -> 404", api(base, "PUT", "/admin/usuarios/9999/papel", {"papel": "ADMIN"}, tm)[0] == 404)
        cb = b.new_context(viewport=W); pb = entrar_ui(cb, "bia")
        ck("admin: item Administração visível", pb.locator('.menu__item[href="admin.html"]').count() == 1)
        pb.click('.menu__item[href="admin.html"]'); pb.wait_for_selector(".adm__item")
        ck("admin: 3 abas, sem Usuários", pb.locator(".adm__aba").all_inner_texts() == ["Módulos", "Questões", "Importar"], pb.locator(".adm__aba").all_inner_texts())
        ck("admin: /admin/usuarios -> 403", api(base, "GET", "/admin/usuarios", None, tb)[0] == 403)
        ck("admin: alterar papel -> 403", api(base, "PUT", f"/admin/usuarios/{idc}/papel", {"papel": "ADMIN"}, tb)[0] == 403)
        ck("admin: não pode se promover a MASTER -> 403", api(base, "PUT", f"/admin/usuarios/{idb}/papel", {"papel": "MASTER"}, tb)[0] == 403)
        pb.click("#novoMod"); pb.fill("#fmT", "Grafos e Caminhos"); pb.fill("#fmD", "BFS, DFS e caminhos mínimos pelo reino."); pb.click(".adm__tema:has(input[value=hash])"); pb.screenshot(path="/tmp/shots/adm_form_modulo.png")
        pb.click('dialog button[type=submit]'); pb.wait_for_selector("dialog", state="detached"); pb.wait_for_selector('.adm__item:has-text("Grafos e Caminhos")')
        mods = api(base, "GET", "/modulos", None, tb)[1]; novo = [m for m in mods if m["titulo"] == "Grafos e Caminhos"][0]
        ck("módulo novo aparece na API pública com o tema escolhido", novo["tema"] == "hash" and novo["quantidadeExercicios"] == 0, novo)
        pm2 = pc; pm2.goto(BASE + "modulos.html"); pm2.wait_for_selector(".missao"); pm2.wait_for_timeout(500)
        card = pm2.locator('.missao:has(h3:has-text("Grafos e Caminhos"))')
        ck("módulo novo aparece na Central com a ilustração do tema (tom do hash)", card.count() == 1 and "6f8fb5" in (card.get_attribute("style") or "") and card.locator("svg.arte-svg").count() == 1, card.get_attribute("style"))
        pm2.screenshot(path="/tmp/shots/adm_central_nova.png")
        pb.fill("#fmT", "x") if False else None
        pb.click('.adm__item:has-text("Grafos e Caminhos") [data-ed]'); pb.fill("#fmT", "Grafos e Redes"); pb.click('dialog button[type=submit]'); pb.wait_for_selector('.adm__item:has-text("Grafos e Redes")')
        ck("editar módulo persiste", any(m["titulo"] == "Grafos e Redes" for m in api(base, "GET", "/modulos", None, tb)[1]))
        pb.click("#novoMod"); pb.fill("#fmT", "A"); pb.fill("#fmD", "x"); pb.click('dialog button[type=submit]'); ck("validação: título curto bloqueado", pb.locator("#fmE").is_visible()); pb.click("dialog [data-n]")
        ck("API: módulo com tema inexistente -> 400", api(base, "POST", "/admin/modulos", {"titulo": "Teste", "descricao": "d", "tema": "ninja"}, tb)[0] == 400)
        ordem = lambda: [m["titulo"] for m in api(base, "GET", "/admin/modulos", None, tb)[1]]
        pb.click('.adm__item:has-text("Grafos e Redes") [data-mv][data-d="-1"]'); pb.wait_for_timeout(400)
        o = ordem(); ck("reordenar módulo (sobe uma posição)", o.index("Grafos e Redes") == len(o) - 2, o)
        pb.click('.adm__item:has-text("Grafos e Redes") [data-q]'); pb.wait_for_selector("#novaQ")
        ck("aba Questões abre no módulo escolhido", pb.eval_on_selector("#selMod", "e=>e.selectedOptions[0].textContent") == "Grafos e Redes")
        pb.click("#novaQ"); pb.fill("#fqD", "Qual estrutura a BFS usa para visitar vizinhos em largura?")
        pb.fill('[data-o="0"]', "Pilha"); pb.fill('[data-o="1"]', "Fila"); pb.click("#fqAdd"); pb.fill('[data-o="2"]', "Heap"); pb.check('input[name=cor][value="1"]')
        pb.fill("#fqE", "A BFS usa fila (FIFO)."); pb.select_option("#fqF", "MEDIO"); pb.fill("#fqP", "25")
        pb.check('input[name=vis][value=svg]'); pb.fill("#fqSvg", svg_ok()); pb.wait_for_selector("#fqPrev img"); pb.screenshot(path="/tmp/shots/adm_form_questao.png")
        pb.click('dialog button[type=submit]'); pb.wait_for_selector(".adm__item--q")
        qs = api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]
        ck("questão criada com todos os campos", len(qs) == 1 and qs[0]["pontos"] == 25 and qs[0]["indiceCorreto"] == 1 and qs[0]["opcoes"] == ["Pilha", "Fila", "Heap"] and qs[0]["visual"].startswith("<svg") and "xmlns" in qs[0]["visual"], qs[0] if qs else "")
        pj = pc; pj.goto(BASE + f"exercicio.html?modulo={novo['id']}"); pj.wait_for_selector(".opcao"); pj.wait_for_timeout(300)
        ck("jogador vê o enunciado e o SVG da questão nova", "BFS" in pj.inner_text("#descricao") and pj.locator("#visual img").count() == 1 and pj.is_visible("#visual"))
        pj.screenshot(path="/tmp/shots/adm_exercicio_visual.png")
        pj.click('.opcao[data-i="0"]'); pj.wait_for_timeout(300); ck("resposta errada recusada", "Tente novamente" in pj.inner_text("#feedback"))
        pj.click('.opcao[data-i="1"]'); pj.wait_for_timeout(400); ck("resposta correta aceita com a explicação cadastrada", "A BFS usa fila" in pj.inner_text("#feedback"), pj.inner_text("#feedback"))
        ck("API pública não vaza a correta/explicação", "indiceCorreto" not in json.dumps(api(base, "GET", f"/modulos/{novo['id']}", None, tc)[1]))
        s, j = api(base, "POST", f"/admin/modulos/{novo['id']}/questoes", {"descricao": "  qual ESTRUTURA a bfs usa para visitar vizinhos em largura? ", "dificuldade": "FACIL", "opcoes": ["a", "b"], "indiceCorreto": 0}, tb)
        ck("duplicata (ignora caixa/espaços) -> 409", s == 409, j)
        ruins = [{"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "opcoes": ["a"], "indiceCorreto": 0}, {"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "opcoes": ["a", "b"], "indiceCorreto": 5},
                 {"descricao": "Enunciado válido aqui", "dificuldade": "XX", "opcoes": ["a", "b"], "indiceCorreto": 0}, {"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "pontos": 0, "opcoes": ["a", "b"], "indiceCorreto": 0},
                 {"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "opcoes": ["a", "b"], "indiceCorreto": 0, "visual": "<svg><script>alert(1)</script></svg>"},
                 {"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "opcoes": ["a", "b"], "indiceCorreto": 0, "visual": '<svg onload="x()"></svg>'},
                 {"descricao": "Enunciado válido aqui", "dificuldade": "FACIL", "opcoes": ["a", "b"], "indiceCorreto": 0, "visual": '<svg><image href="http://evil/x.png"/></svg>'}]
        ck("API: 7 questões inválidas/maliciosas -> 400", all(api(base, "POST", f"/admin/modulos/{novo['id']}/questoes", q, tb)[0] == 400 for q in ruins))
        pb.click("#novaQ"); pb.fill("#fqD", "Questão com SVG malicioso para testar"); pb.fill('[data-o="0"]', "a"); pb.fill('[data-o="1"]', "b"); pb.check('input[name=vis][value=svg]'); pb.fill("#fqSvg", "<svg><script>alert(1)</script></svg>")
        pb.click('dialog button[type=submit]'); pb.wait_for_selector("#fqErro:visible"); ck("UI: SVG malicioso recusado com mensagem", "não permitido" in pb.inner_text("#fqErro"), pb.inner_text("#fqErro"))
        pb.click("dialog [data-n]")
        im = Image.new("RGB", (1600, 900), (20, 80, 160)); bio = io.BytesIO(); im.save(bio, "PNG")
        pb.click("#novaQ"); pb.fill("#fqD", "Questão com imagem grande reduzida no envio"); pb.fill('[data-o="0"]', "a"); pb.fill('[data-o="1"]', "b"); pb.check('input[name=vis][value=imagem]')
        pb.set_input_files("#fqArq", {"name": "g.png", "mimeType": "image/png", "buffer": bio.getvalue()}); pb.wait_for_selector("#fqPrev img"); pb.click('dialog button[type=submit]'); pb.wait_for_selector("dialog", state="detached")
        q2 = [q for q in api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1] if "imagem grande" in q["descricao"]][0]
        ck("imagem grande é reduzida e salva como JPEG ≤300 KB", q2["visual"].startswith("data:image/jpeg") and len(q2["visual"]) * 3 / 4 < 300 * 1024)
        pb.click('.adm__item--q:has-text("BFS") [data-ed]'); pb.fill("#fqE", "Explicação revisada."); pb.click('dialog button[type=submit]'); pb.wait_for_selector("dialog", state="detached")
        ck("editar questão persiste", api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1][0]["explicacao"] == "Explicação revisada.")
        pb.click('.adm__item--q:has-text("imagem grande") [data-mv][data-d="-1"]'); pb.wait_for_timeout(400)
        ck("reordenar questão", "imagem grande" in api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1][0]["descricao"])
        pb.click('.adm__item--q:has-text("imagem grande") [data-ex]'); pb.wait_for_selector("dialog[open]"); pb.click("dialog [data-n]"); pb.wait_for_timeout(200)
        ck("excluir: cancelar mantém a questão", len(api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]) == 2)
        pb.click('.adm__item--q:has-text("imagem grande") [data-ex]'); pb.click("dialog [data-s]"); pb.wait_for_timeout(400)
        ck("excluir: confirmar remove a questão", len(api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]) == 1)
        aba(pb, "importar"); pb.select_option("#impMod", str(novo["id"]))
        def arquivo(obj, nome="q.json"): return {"name": nome, "mimeType": "application/json", "buffer": json.dumps(obj).encode()}
        q = lambda d, **k: dict({"descricao": d, "dificuldade": "FACIL", "opcoes": ["Sim", "Não"], "indiceCorreto": 0}, **k)
        bom = {"modulo": "Grafos e Redes", "questoes": [q("O que é um grafo dirigido?"), q("A DFS usa pilha ou recursão?", pontos=15, explicacao="Pilha."), q("Qual estrutura a BFS usa para visitar vizinhos em largura?"), q("O que é um ciclo em um grafo?", visual=svg_ok())]}
        pb.set_input_files("#impArq", arquivo(bom)); pb.wait_for_selector(".adm__resumo"); pb.screenshot(path="/tmp/shots/adm_importar.png")
        txt = pb.inner_text(".adm__resumo"); import re as _re
        ck("importação: validação mostra 4 no arquivo, 3 válidas, 1 duplicada, 0 erros", _re.sub(r"\s+", " ", txt) == "4 no arquivo 3 válidas 1 duplicadas 0 erros", _re.sub(r"\s+", " ", txt))
        ck("importação: prévia lista as questões válidas", pb.locator(".adm__previa li").count() == 3)
        ck("importação: validar NÃO grava nada", len(api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]) == 1)
        pb.click("#impOk"); pb.wait_for_selector("dialog[open]"); pb.click("dialog [data-s]"); pb.wait_for_selector(".adm__res .estado:has-text('importadas')")
        qs = api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]
        ck("importação confirmada grava 3 questões no módulo certo", len(qs) == 4 and all(x["moduloId"] == novo["id"] for x in qs))
        ck("importadas já visíveis ao jogador", len(api(base, "GET", f"/modulos/{novo['id']}", None, tc)[1]["exercicios"]) == 4)
        ck("importação: pontos padrão por dificuldade (10) e visual salvo", [x for x in qs if "grafo dirigido" in x["descricao"]][0]["pontos"] == 10 and [x for x in qs if "ciclo" in x["descricao"]][0]["visual"].startswith("<svg"))
        antes = len(qs)
        ruim = {"questoes": [q("Questão boa que não deve entrar sozinha"), {"descricao": "x", "dificuldade": "FACIL", "opcoes": ["a"], "indiceCorreto": 3}, q("Outra boa que também fica de fora", visual="<svg><script/></svg>")]}
        pb.set_input_files("#impArq", arquivo(ruim)); pb.wait_for_selector(".adm__erros")
        ck("arquivo com erros: lista erros por questão e desabilita Importar", pb.locator(".adm__erros li").count() == 2 and pb.is_disabled("#impOk"), pb.locator(".adm__erros li").all_inner_texts())
        s, j = api(base, "POST", f"/admin/modulos/{novo['id']}/importacao", dict(ruim, confirmar=True), tb)
        ck("API: confirmar com erros NÃO grava nada (atômico)", s == 200 and j["importado"] is False and len(api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[1]) == antes, (s, j["importado"]))
        pb.set_input_files("#impArq", arquivo(dict(bom, modulo="Tabelas Hash"))); pb.wait_for_selector(".adm__erros")
        ck("módulo do arquivo diferente do destino é bloqueado", "destino" in pb.inner_text(".adm__erros") and pb.is_disabled("#impOk"), pb.inner_text(".adm__erros"))
        pb.set_input_files("#impArq", {"name": "x.json", "mimeType": "application/json", "buffer": b"{ nao eh json"}); pb.wait_for_selector("#impRes .adm__erro")
        ck("JSON malformado -> mensagem clara", "JSON" in pb.inner_text("#impRes"))
        pb.set_input_files("#impArq", arquivo({"foo": 1})); pb.wait_for_selector("#impRes .adm__erro"); ck("estrutura inesperada -> mensagem clara", "Formato inválido" in pb.inner_text("#impRes"))
        pb.set_input_files("#impArq", arquivo([q(f"Questão em lista simples número {i}") for i in range(2)])); pb.wait_for_selector(".adm__resumo"); ck("aceita lista simples de questões", pb.locator(".adm__previa li").count() == 2)
        ck("API: 501 questões -> 400", api(base, "POST", f"/admin/modulos/{novo['id']}/importacao", {"questoes": [q(f"Questão número {i} para limite") for i in range(501)]}, tb)[0] == 400)
        ck("API: importar para módulo inexistente -> 404", api(base, "POST", "/admin/modulos/99999/importacao", bom, tb)[0] == 404)
        ck("API: importação sem papel -> 403", api(base, "POST", f"/admin/modulos/{novo['id']}/importacao", bom, tc)[0] == 403)
        aba(pb, "modulos"); pb.click('.adm__item:has-text("Grafos e Redes") [data-ed]'); pb.uncheck("#fmA"); pb.click('dialog button[type=submit]'); pb.wait_for_selector('.adm__item.is-inativo')
        ck("módulo inativo some para o jogador", all(m["titulo"] != "Grafos e Redes" for m in api(base, "GET", "/modulos", None, tc)[1]) and api(base, "GET", f"/modulos/{novo['id']}", None, tc)[0] == 404)
        pb.click('.adm__item:has-text("Grafos e Redes") [data-ex]'); pb.wait_for_selector("dialog[open]"); pb.click("dialog [data-n]")
        ck("excluir módulo: cancelar mantém", any(m["titulo"] == "Grafos e Redes" for m in api(base, "GET", "/admin/modulos", None, tb)[1]))
        pb.click('.adm__item:has-text("Grafos e Redes") [data-ex]'); pb.click("dialog [data-s]"); pb.wait_for_timeout(400)
        ck("excluir módulo: confirmar remove módulo e questões", all(m["titulo"] != "Grafos e Redes" for m in api(base, "GET", "/admin/modulos", None, tb)[1]) and api(base, "GET", f"/admin/modulos/{novo['id']}/questoes", None, tb)[0] == 404)
        pm.locator('.adm__lin:has-text("bia") button').click(); pm.click("dialog [data-s]"); pm.wait_for_timeout(400)
        ck("master rebaixa bia para USUARIO", papel_bia() == "USUARIO")
        ck("ex-admin perde acesso na hora (mesmo token) -> 403", api(base, "GET", "/admin/modulos", None, tb)[0] == 403)
        pb.reload(); pb.wait_for_url("**/modulos.html", timeout=5000); ck("ex-admin: admin.html redireciona", True)
        pm2.goto(BASE + "modulos.html"); pm2.wait_for_selector(".missao")
        ck("módulos existentes continuam com a arte deduzida do título", pm2.locator('.missao:has(h3:has-text("Algoritmos de Busca"))').get_attribute("style").find("b89a5e") > 0)
        mc = b.new_context(viewport={"width": 360, "height": 740}, device_scale_factor=2, is_mobile=True, has_touch=True); mp = entrar_ui(mc, "mestre", "senha-master-1")
        mp.goto(BASE + "admin.html"); mp.wait_for_selector(".adm__item"); mp.wait_for_timeout(300)
        ck("mobile: aba Módulos sem overflow horizontal", mp.evaluate("document.documentElement.scrollWidth<=innerWidth && document.querySelector('.adm__painel').scrollWidth<=document.querySelector('.adm__painel').clientWidth+1"))
        mp.screenshot(path="/tmp/shots/adm_mobile_modulos.png")
        mp.click("#novoMod"); mp.wait_for_selector("dialog[open]"); mp.wait_for_timeout(250); mp.screenshot(path="/tmp/shots/adm_mobile_form.png")
        ck("mobile: formulário de módulo sem overflow", mp.evaluate("(()=>{const c=document.querySelector('.adm__caixa');return c.scrollWidth<=c.clientWidth+1})()")); mp.click("dialog [data-n]")
        aba(mp, "questoes"); mp.click("#novaQ"); mp.wait_for_selector("dialog[open]"); mp.wait_for_timeout(250)
        ck("mobile: formulário de questão sem overflow", mp.evaluate("(()=>{const c=document.querySelector('.adm__caixa');return c.scrollWidth<=c.clientWidth+1&&document.documentElement.scrollWidth<=innerWidth})()")); mp.screenshot(path="/tmp/shots/adm_mobile_questao.png"); mp.click("dialog [data-n]")
        aba(mp, "usuarios"); ck("mobile: tabela de usuários sem overflow", mp.evaluate("document.documentElement.scrollWidth<=innerWidth && document.querySelector('.adm__painel').scrollWidth<=document.querySelector('.adm__painel').clientWidth+1"))
        mp.screenshot(path="/tmp/shots/adm_mobile_usuarios.png"); b.close()
finally: srv.terminate()
print(f"\n{sum(R)}/{len(R)} passaram"); sys.exit(0 if all(R) else 1)
