import base64, re, xml.etree.ElementTree as ET

TEMAS = {"arvore", "busca", "ordenacao", "hash", "analise", "padrao"}
ELEMS = {"svg","g","path","circle","ellipse","rect","line","polyline","polygon","text","tspan","defs","lineargradient","radialgradient","stop","title","desc","marker","clippath"}
NS = "http://www.w3.org/2000/svg"
MODULOS = []; QUESTOES = {}; _prox = {"q": 10000, "m": 100}

def semear(mods, ex):
    for i, t, d, p, q in mods:
        MODULOS.append({"id": i, "titulo": t, "descricao": d, "tema": None, "ativo": True, "ordem": len(MODULOS) + 1, "pct": p})
        for k, (desc, ops, c, dif, pts) in enumerate(ex):
            QUESTOES[i * 100 + k] = {"id": i * 100 + k, "modulo": i, "ordem": k + 1, "descricao": desc, "dificuldade": dif, "pontos": pts,
                                     "opcoes": ops, "indiceCorreto": c, "explicacao": "A busca binária divide o espaço de busca pela metade a cada passo.", "visual": None}

def chave(s): return re.sub(r"\s+", " ", (s or "").strip().lower())

def valida_visual(v):
    if v is None or not str(v).strip(): return None
    v = v.strip()
    if v.startswith("data:"):
        m = re.match(r"data:(image/(?:jpeg|png|webp));base64,(.*)$", v, re.S)
        if not m: raise ValueError("Formato não suportado. Use JPEG, PNG ou WebP.")
        try: b = base64.b64decode(m.group(2), validate=True)
        except Exception: raise ValueError("Imagem inválida.")
        if not b or len(b) > 300 * 1024: raise ValueError("Imagem grande demais (máximo 300 KB).")
        ok = {"image/jpeg": b[:3] == b"\xff\xd8\xff", "image/png": b[:4] == b"\x89PNG", "image/webp": b[:4] == b"RIFF" and b[8:12] == b"WEBP"}[m.group(1)]
        if not ok: raise ValueError("O arquivo não corresponde a uma imagem válida.")
        return v
    if len(v) > 30000: raise ValueError("SVG grande demais (máximo 30.000 caracteres).")
    if not v[:4].lower() == "<svg": raise ValueError("O SVG deve começar com <svg ...>.")
    if "<!doctype" in v.lower() or "<!entity" in v.lower() or "<![cdata[" in v.lower(): raise ValueError("SVG inválido (XML malformado ou com DOCTYPE).")
    try: raiz = ET.fromstring(v)
    except Exception: raise ValueError("SVG inválido (XML malformado ou com DOCTYPE).")
    def tag(e): return e.tag.split("}")[-1]
    def conferir(e):
        if tag(e).lower() not in ELEMS: raise ValueError("Elemento SVG não permitido: <%s>." % tag(e))
        for a, val in e.attrib.items():
            la = a.split("}")[-1].lower(); lv = re.sub(r"\s+", "", val.lower())
            if la.startswith("on"): raise ValueError("Atributo de evento não permitido: %s." % a)
            if la == "href" and not val.startswith("#"): raise ValueError("Referências externas não são permitidas no SVG.")
            if any(x in lv for x in ("javascript:", "data:", "http:", "https:", "//")): raise ValueError("Valor de atributo não permitido no SVG (%s)." % a)
        for f in e: conferir(f)
    if tag(raiz) != "svg": raise ValueError("O elemento raiz deve ser <svg>.")
    conferir(raiz)
    if "xmlns=" not in v[:200]: v = '<svg xmlns="%s"%s' % (NS, v[4:])
    return v

def valida_questao(q):
    if not isinstance(q, dict): raise ValueError("Questão vazia.")
    d = (q.get("descricao") or "").strip()
    if not 5 <= len(d) <= 1000: raise ValueError("O enunciado deve ter de 5 a 1000 caracteres.")
    dif = (q.get("dificuldade") or "").strip().upper()
    if dif not in ("FACIL", "MEDIO", "DIFICIL"): raise ValueError("Dificuldade inválida (use FACIL, MEDIO ou DIFICIL).")
    pts = q.get("pontos"); pts = {"FACIL": 10, "MEDIO": 20, "DIFICIL": 30}[dif] if pts is None else pts
    if not isinstance(pts, int) or isinstance(pts, bool) or not 1 <= pts <= 1000: raise ValueError("Pontos devem ficar entre 1 e 1000.")
    ops = q.get("opcoes")
    if not isinstance(ops, list) or not 2 <= len(ops) <= 6: raise ValueError("Informe de 2 a 6 alternativas.")
    limpas, vistas = [], set()
    for o in ops:
        t = o.strip() if isinstance(o, str) else ""
        if not t or len(t) > 500: raise ValueError("Cada alternativa deve ter de 1 a 500 caracteres.")
        if t.lower() in vistas: raise ValueError("Há alternativas repetidas.")
        vistas.add(t.lower()); limpas.append(t)
    c = q.get("indiceCorreto")
    if not isinstance(c, int) or isinstance(c, bool) or not 0 <= c < len(limpas): raise ValueError("Indique a alternativa correta (índice dentro das alternativas).")
    e = (q.get("explicacao") or "").strip() or None
    if e and len(e) > 1000: raise ValueError("A explicação deve ter até 1000 caracteres.")
    return {"descricao": d, "dificuldade": dif, "pontos": pts, "opcoes": limpas, "indiceCorreto": c, "explicacao": e, "visual": valida_visual(q.get("visual"))}

def valida_modulo(d):
    d = d or {}
    t = (d.get("titulo") or "").strip(); desc = (d.get("descricao") or "").strip(); tema = d.get("tema") or None
    if not 2 <= len(t) <= 120: return "O título deve ter de 2 a 120 caracteres."
    if not 1 <= len(desc) <= 500: return "A descrição é obrigatória (até 500 caracteres)."
    if tema and tema not in TEMAS: return "Tema de ilustração desconhecido."
    return None

def pub_modulo(m): return {"id": m["id"], "titulo": m["titulo"], "descricao": m["descricao"], "tema": m["tema"], "ativo": m["ativo"], "ordem": m["ordem"], "quantidade": sum(1 for q in QUESTOES.values() if q["modulo"] == m["id"])}
def pub_q(q): return {"id": q["id"], "moduloId": q["modulo"], "ordem": q["ordem"], "descricao": q["descricao"], "dificuldade": q["dificuldade"], "pontos": q["pontos"], "opcoes": q["opcoes"], "indiceCorreto": q["indiceCorreto"], "explicacao": q["explicacao"], "visual": q["visual"]}
def _modulo(i): return next((m for m in MODULOS if m["id"] == i), None)
def _qs(mid): return sorted([q for q in QUESTOES.values() if q["modulo"] == mid], key=lambda q: (q["ordem"], q["id"]))
def _renumerar(lista):
    for i, x in enumerate(lista): x["ordem"] = i + 1
def _mover(lista, alvo, direcao):
    if direcao not in (-1, 1): raise ValueError("Direção inválida (use -1 ou 1).")
    i = lista.index(alvo); j = i + direcao
    if 0 <= j < len(lista): lista[i], lista[j] = lista[j], lista[i]
    _renumerar(lista)

def publico_modulos(): return [dict(id=m["id"], titulo=m["titulo"], descricao=m["descricao"], tema=m["tema"], porcentagem=m.get("pct", 0), quantidadeExercicios=pub_modulo(m)["quantidade"]) for m in sorted(MODULOS, key=lambda m: m["ordem"]) if m["ativo"]]
def publico_modulo(i):
    m = _modulo(i)
    if not m or not m["ativo"]: return None
    return {"id": m["id"], "titulo": m["titulo"], "descricao": m["descricao"], "tema": m["tema"],
            "exercicios": [{"id": q["id"], "descricao": q["descricao"], "opcoes": q["opcoes"], "dificuldade": q["dificuldade"], "pontos": q["pontos"], "visual": q["visual"]} for q in _qs(i)]}
def responder(eid, r):
    q = QUESTOES.get(eid)
    if not q or not (_modulo(q["modulo"]) or {}).get("ativo"): return 404, {"erro": "Exercício não encontrado."}
    ok = r == q["indiceCorreto"]
    return 200, {"correta": ok, "feedback": (q["explicacao"] or "") if ok else ""}

def tratar(metodo, caminho, corpo, usuario, usuarios):
    if not caminho.startswith("/api/admin"): return None
    if not usuario: return 401, {"erro": "Autenticação necessária."}
    papel = usuario["papel"]; conteudo = papel in ("ADMIN", "MASTER")
    p = caminho[len("/api/admin"):]
    try:
        if p.startswith("/usuarios"):
            if papel != "MASTER": return 403, {"erro": "Acesso exclusivo do Master."}
            if metodo == "GET" and p == "/usuarios":
                return 200, [{"id": u["id"], "nomeUsuario": u["nome"], "email": None, "papel": u["papel"], "nivel": u["nivel"], "pontuacao": u["pontuacao"], "criadoEm": "2026-01-01T00:00:00Z"} for u in usuarios.values()]
            m = re.fullmatch(r"/usuarios/(\d+)/papel", p)
            if metodo == "PUT" and m:
                novo = (corpo.get("papel") or "").strip().upper()
                if novo not in ("USUARIO", "ADMIN", "MASTER"): return 400, {"erro": "Papel inválido (use USUARIO ou ADMIN)."}
                alvo = next((u for u in usuarios.values() if u["id"] == int(m.group(1))), None)
                if not alvo: return 404, {"erro": "Usuário não encontrado."}
                if usuario["id"] == alvo["id"]: return 403, {"erro": "Você não pode alterar o seu próprio papel."}
                if alvo["papel"] == "MASTER": return 403, {"erro": "A conta Master é protegida e não pode ser alterada."}
                if novo == "MASTER": return 403, {"erro": "Não é possível promover alguém a Master."}
                alvo["papel"] = novo
                return 200, {"id": alvo["id"], "nomeUsuario": alvo["nome"], "email": None, "papel": novo, "nivel": alvo["nivel"], "pontuacao": alvo["pontuacao"], "criadoEm": "2026-01-01T00:00:00Z"}
            return 404, {}
        if not conteudo: return 403, {"erro": "Acesso restrito a administradores."}
        if metodo == "GET" and p == "/modulos": return 200, [pub_modulo(m) for m in sorted(MODULOS, key=lambda m: m["ordem"])]
        if metodo == "POST" and p == "/modulos":
            e = valida_modulo(corpo)
            if e: return 400, {"erro": e}
            _prox["m"] += 1
            m = {"id": _prox["m"], "titulo": corpo["titulo"].strip(), "descricao": corpo["descricao"].strip(), "tema": corpo.get("tema") or None, "ativo": corpo.get("ativo") is not False, "ordem": len(MODULOS) + 1}
            MODULOS.append(m); return 201, pub_modulo(m)
        mm = re.fullmatch(r"/modulos/(\d+)", p)
        if mm:
            m = _modulo(int(mm.group(1)))
            if not m: return 404, {"erro": "Módulo não encontrado."}
            if metodo == "PUT":
                e = valida_modulo(corpo)
                if e: return 400, {"erro": e}
                m.update(titulo=corpo["titulo"].strip(), descricao=corpo["descricao"].strip(), tema=corpo.get("tema") or None, ativo=m["ativo"] if corpo.get("ativo") is None else bool(corpo["ativo"]))
                return 200, pub_modulo(m)
            if metodo == "DELETE":
                for k in [k for k, q in QUESTOES.items() if q["modulo"] == m["id"]]: del QUESTOES[k]
                MODULOS.remove(m); _renumerar(MODULOS); return 204, None
        mm = re.fullmatch(r"/modulos/(\d+)/mover", p)
        if metodo == "POST" and mm:
            m = _modulo(int(mm.group(1)))
            if not m: return 404, {"erro": "Módulo não encontrado."}
            lista = sorted(MODULOS, key=lambda x: x["ordem"]); _mover(lista, m, corpo.get("direcao"))
            return 200, [pub_modulo(x) for x in sorted(MODULOS, key=lambda x: x["ordem"])]
        mm = re.fullmatch(r"/modulos/(\d+)/questoes", p)
        if mm:
            m = _modulo(int(mm.group(1)))
            if not m: return 404, {"erro": "Módulo não encontrado."}
            if metodo == "GET": return 200, [pub_q(q) for q in _qs(m["id"])]
            if metodo == "POST":
                q = valida_questao(corpo)
                if chave(q["descricao"]) in {chave(x["descricao"]) for x in _qs(m["id"])}: return 409, {"erro": "Já existe uma questão com este enunciado neste módulo."}
                _prox["q"] += 1; novo = dict(q, id=_prox["q"], modulo=m["id"], ordem=len(_qs(m["id"])) + 1); QUESTOES[novo["id"]] = novo
                return 201, pub_q(novo)
        mm = re.fullmatch(r"/modulos/(\d+)/importacao", p)
        if metodo == "POST" and mm:
            m = _modulo(int(mm.group(1)))
            if not m: return 404, {"erro": "Módulo não encontrado."}
            itens = corpo.get("questoes")
            if not isinstance(itens, list) or not itens: return 400, {"erro": "O arquivo não contém questões."}
            if len(itens) > 500: return 400, {"erro": "Máximo de 500 questões por importação."}
            erros = []; ignorar = corpo.get("ignorarDuplicadas") is not False
            dest = (corpo.get("modulo") or "").strip()
            if dest and dest.lower() != m["titulo"].lower() and dest != str(m["id"]):
                erros.append({"indice": 0, "mensagem": 'O arquivo é do módulo "%s", mas o destino escolhido é "%s".' % (dest, m["titulo"])})
            vistas = {chave(x["descricao"]) for x in _qs(m["id"])}; aceitas = []; dup = 0
            for i, it in enumerate(itens):
                try:
                    q = valida_questao(it)
                    if chave(q["descricao"]) in vistas:
                        dup += 1
                        if not ignorar: erros.append({"indice": i + 1, "mensagem": "Enunciado duplicado."})
                        continue
                    vistas.add(chave(q["descricao"])); aceitas.append(q)
                except ValueError as ex: erros.append({"indice": i + 1, "mensagem": str(ex)})
            gravar = corpo.get("confirmar") is True and not erros and bool(aceitas)
            if gravar:
                for q in aceitas:
                    _prox["q"] += 1; QUESTOES[_prox["q"]] = dict(q, id=_prox["q"], modulo=m["id"], ordem=len(_qs(m["id"])) + 1)
            return 200, {"total": len(itens), "validas": len(aceitas), "duplicadas": dup, "importadas": len(aceitas) if gravar else 0, "importado": gravar, "erros": erros,
                         "previa": [pub_q(dict(q, id=0, modulo=m["id"], ordem=0)) for q in aceitas]}
        mm = re.fullmatch(r"/questoes/(\d+)(/mover)?", p)
        if mm:
            q = QUESTOES.get(int(mm.group(1)))
            if not q: return 404, {"erro": "Questão não encontrada."}
            if mm.group(2) and metodo == "POST":
                lista = _qs(q["modulo"]); _mover(lista, q, corpo.get("direcao")); return 200, [pub_q(x) for x in _qs(q["modulo"])]
            if metodo == "PUT":
                n = valida_questao(corpo)
                if any(x["id"] != q["id"] and chave(x["descricao"]) == chave(n["descricao"]) for x in _qs(q["modulo"])): return 409, {"erro": "Já existe uma questão com este enunciado neste módulo."}
                q.update(n); return 200, pub_q(q)
            if metodo == "DELETE":
                del QUESTOES[q["id"]]; _renumerar(_qs(q["modulo"])); return 204, None
    except ValueError as e:
        return 400, {"erro": str(e)}
    return 404, {}
