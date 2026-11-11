import json, re, sys, threading, os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer, SimpleHTTPRequestHandler
MODS = [
 (1,"Análise de Algoritmos","Notação Big O: compare O(1), O(n) e O(log n) e descubra como o custo cresce.",100,6),
 (2,"Algoritmos de Busca","Busca sequencial contra busca binária: encontre valores sem percorrer o reino inteiro.",60,5),
 (3,"Algoritmos de Ordenação","Bubble, Selection, Insertion, Merge e Quick Sort: coloque o caos em ordem.",0,7),
 (4,"Tabelas Hash","Funções de espalhamento, colisões e endereçamento: encontre qualquer tesouro no acervo.",0,5),
 (5,"Estruturas de Dados","Árvores binárias, de busca e balanceamento: domine as ramificações do conhecimento.",0,6)]
EX = [
 ("Você tem 1.000.000 de elementos ordenados. Qual estratégia encontra um valor com menos comparações?",["Busca sequencial","Busca binária","Ordenar de novo a cada consulta","Sortear posições ao acaso"],1,"FACIL",10),
 ("Qual é a complexidade da busca binária no pior caso?",["O(n)","O(1)","O(log n)","O(n²)"],2,"MEDIO",20),
 ("Uma lista com 1.024 elementos ordenados. No máximo quantas comparações a busca binária faz?",["10 ou 11","512","1.024","32"],0,"MEDIO",20),
 ("Em uma busca binária, o que acontece quando o valor procurado é menor que o elemento do meio?",["Descarta-se a metade esquerda","Descarta-se a metade direita","Recomeça-se do início","A busca termina sem resultado"],1,"DIFICIL",30),
 ("Qual pré-condição é indispensável para aplicar a busca binária?",["Elementos únicos","Quantidade par de elementos","Coleção ordenada","Valores numéricos"],2,"FACIL",10)]
def ex_list(mid):
    return [{"id":mid*100+i,"descricao":d,"opcoes":o,"dificuldade":dif,"pontos":p} for i,(d,o,c,dif,p) in enumerate(EX)]
import base64, random, secrets
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mock_admin
mock_admin.semear(MODS, EX)
USERS = {}; TOKENS = {}
USERS["mestre"]={"id":1,"nome":"mestre","senha":"senha-master-1","pontuacao":0,"xp":0,"nivel":1,"avatar":None,"img":None,"papel":"MASTER"}
_java = open(os.path.join(os.getcwd(),"src","main","java","algoquest","model","AvatarIds.java"),encoding="utf-8").read()
def _grupo(n): return re.findall(r'"([a-z0-9_]+)"', re.search(n+r" = \{(.*?)\};", _java, re.S).group(1))
FEM = _grupo("FEMININOS"); MASC = _grupo("MASCULINOS")
CAT = None
def catalogo():
    global CAT
    if CAT is None:
        txt = open(os.path.join(os.getcwd(),"frontend","js","avatares-catalogo.js"),encoding="utf-8").read()
        CAT = re.findall(r'"id": "([a-z0-9_]+)"', txt)
    return CAT
def padrao(nome): return random.choice(FEM if nome.strip().lower().endswith("a") else MASC)
def valida_img(d):
    m = re.match(r"data:(image/(?:jpeg|png|webp));base64,(.*)$", d or "", re.S)
    if not m: return "Formato não suportado. Use JPEG, PNG ou WebP."
    try: b = base64.b64decode(m.group(2), validate=True)
    except Exception: return "Imagem inválida."
    if not b or len(b) > 150*1024: return "Imagem grande demais (máximo 150 KB)."
    ok = {"image/jpeg": b[:3]==b"\xff\xd8\xff", "image/png": b[:4]==b"\x89PNG", "image/webp": b[:4]==b"RIFF" and b[8:12]==b"WEBP"}[m.group(1)]
    return None if ok else "O arquivo não corresponde a uma imagem válida."
def pub(u, token=None):
    r = {"id":u["id"],"nomeUsuario":u["nome"],"pontuacao":u["pontuacao"],"xp":u["xp"],"nivel":u["nivel"],"xpProximoNivel":u["nivel"]*100,"avatar":u["avatar"],"papel":u["papel"]}
    if u["avatar"]=="personalizado": r["avatarImagem"]=u["img"]
    if token: r["token"]=token
    return r
def abrir(u):
    t = secrets.token_urlsafe(24); TOKENS[t] = u["nome"]; return pub(u, t)
class API(BaseHTTPRequestHandler):
    def _user(self):
        h = self.headers.get("Authorization") or ""
        return USERS.get(TOKENS.get(h[7:])) if h.lower().startswith("bearer ") else None
    def _s(self,code=200,body=None):
        self.send_response(code); self.send_header("Access-Control-Allow-Origin","*"); self.send_header("Access-Control-Allow-Headers","*"); self.send_header("Access-Control-Allow-Methods","*")
        if body is not None: self.send_header("Content-Type","application/json")
        self.end_headers()
        if body is not None: self.wfile.write(json.dumps(body).encode())
    def do_OPTIONS(self): self._s(204)
    def do_GET(self):
        r=mock_admin.tratar("GET",self.path,{},self._user(),USERS)
        if r: return self._s(*r)
        if self.path=="/api/modulos": return self._s(200,mock_admin.publico_modulos())
        if self.path=="/api/auth/me":
            u=self._user()
            return self._s(200,pub(u)) if u else self._s(401,{"erro":"Autenticação necessária."})
        m=re.match(r"/api/modulos/(\d+)$",self.path)
        if m:
            r=mock_admin.publico_modulo(int(m.group(1)))
            return self._s(200,r) if r else self._s(404,{"erro":"Módulo não encontrado."})
        self._s(404,{})
    def do_POST(self):
        n=int(self.headers.get("Content-Length") or 0); raw=self.rfile.read(n).decode() if n else "{}"
        r=mock_admin.tratar("POST",self.path,json.loads(raw or "{}"),self._user(),USERS)
        if r: return self._s(*r)
        if self.path=="/api/auth/registrar":
            d=json.loads(raw or "{}"); nome=(d.get("nomeUsuario") or "").strip(); senha=d.get("senha") or ""
            if not re.fullmatch(r"[\w.-]{3,50}", nome): return self._s(400,{"erro":"Usuário: 3 a 50 caracteres (letras, números, ponto, hífen ou sublinhado)."})
            if not 8<=len(senha)<=128: return self._s(400,{"erro":"A senha deve ter de 8 a 128 caracteres."})
            if nome in USERS: return self._s(409,{"erro":"Este nome de usuário já está em uso."})
            USERS[nome]={"id":len(USERS)+1,"nome":nome,"senha":senha,"pontuacao":0,"xp":0,"nivel":1,"avatar":padrao(nome),"img":None,"papel":"USUARIO"}
            return self._s(201,abrir(USERS[nome]))
        if self.path=="/api/auth/login":
            d=json.loads(raw or "{}")
            if d.get("senha")=="lenta": import time; time.sleep(1.2)
            if d.get("senha")=="errada": return self._s(401,{})
            nome=d.get("nomeUsuario"); u=USERS.get(nome)
            if u is None:
                u=USERS[nome]={"id":len(USERS)+1,"nome":nome,"senha":d.get("senha"),"pontuacao":120,"xp":340,"nivel":3,"avatar":None,"img":None,"papel":"USUARIO"}
            elif u["senha"]!=d.get("senha"): return self._s(401,{"erro":"Usuário ou senha incorretos."})
            if u["avatar"] is None: u["avatar"]=padrao(nome)
            return self._s(200,abrir(u))
        if self.path in ("/api/auth/me/avatar","/api/auth/me/avatar/personalizado"):
            u=self._user()
            if not u: return self._s(401,{"erro":"Autenticação necessária."})
            d=json.loads(raw or "{}")
            if self.path.endswith("personalizado"):
                e=valida_img(d.get("imagem"))
                if e: return self._s(400,{"erro":e})
                u["avatar"]="personalizado"; u["img"]=d["imagem"]
            else:
                if d.get("avatar") not in catalogo(): return self._s(400,{"erro":"Avatar desconhecido."})
                u["avatar"]=d["avatar"]; u["img"]=None
            return self._s(200,pub(u))
        if self.path=="/api/auth/logout": return self._s(200)
        m=re.match(r"/api/modulos/exercicios/(\d+)/resposta$",self.path)
        if m:
            return self._s(*mock_admin.responder(int(m.group(1)),json.loads(raw).get("resposta")))
        self._s(404,{})
    def _meth(self,met):
        n=int(self.headers.get("Content-Length") or 0); raw=self.rfile.read(n).decode() if n else "{}"
        r=mock_admin.tratar(met,self.path,json.loads(raw or "{}"),self._user(),USERS)
        self._s(*r) if r else self._s(404,{})
    def do_PUT(self): self._meth("PUT")
    def do_DELETE(self): self._meth("DELETE")
    def log_message(self,*a): pass
class Static(SimpleHTTPRequestHandler):
    def __init__(s,*a,**k): super().__init__(*a,directory=os.path.join(os.getcwd(),"frontend"),**k)
    def log_message(self,*a): pass
    def end_headers(self): self.send_header("Cache-Control","no-store"); super().end_headers()
if __name__=="__main__":
    threading.Thread(target=ThreadingHTTPServer(("127.0.0.1",8080),API).serve_forever,daemon=True).start()
    print("API :8080  |  frontend :5500"); ThreadingHTTPServer(("127.0.0.1",5500),Static).serve_forever()
