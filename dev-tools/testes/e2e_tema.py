import subprocess, time, sys, io, base64, os
from playwright.sync_api import sync_playwright
from PIL import Image
os.makedirs("/tmp/shots", exist_ok=True)
srv = subprocess.Popen([sys.executable, "dev-tools/mock-api/mock-api.py"]); time.sleep(1.2)
BASE = "http://127.0.0.1:5500/"; R = []
def ck(n, c, x=""): R.append(bool(c)); print(("PASS " if c else "FAIL ") + n + (" -> " + str(x) if x else ""))
TOK = "(n)=>getComputedStyle(document.documentElement).getPropertyValue(n).trim()"
def lum(h):
    h = h.lstrip("#"); r, g, b = [int(h[i:i+2], 16) / 255 for i in (0, 2, 4)]
    f = lambda c: c / 12.92 if c <= .03928 else ((c + .055) / 1.055) ** 2.4
    return .2126 * f(r) + .7152 * f(g) + .0722 * f(b)
def contraste(a, b): la, lb = sorted([lum(a), lum(b)], reverse=True); return (la + .05) / (lb + .05)
def tema(pg): return pg.evaluate("document.documentElement.dataset.tema||null")
def tok(pg, n): return pg.evaluate(TOK, n)
def escolher(pg, cat, id_):
    pg.click("#btnAvatar"); pg.wait_for_selector("dialog.av[open]"); pg.click(f".av__aba[data-cat={cat}]"); pg.click(f".av__card[data-id={id_}]"); pg.click("#avConfirmar"); pg.wait_for_selector("dialog.av", state="detached")
def png(w=500, h=500):
    im = Image.new("RGB", (w, h), (40, 140, 90)); b = io.BytesIO(); im.save(b, "PNG"); return b.getvalue()
try:
    with sync_playwright() as p:
        b = p.chromium.launch(); ctx = b.new_context(viewport={"width": 1366, "height": 800}); pg = ctx.new_page(); erros = []; pg.on("pageerror", lambda e: erros.append(str(e)))
        pg.goto(BASE + "index.html"); pg.click("#btnModo"); pg.fill("#usuario", "Tester"); pg.fill("#senha", "senha-forte-1"); pg.click("#btnEntrar"); pg.wait_for_url("**/modulos.html"); pg.wait_for_selector("#retrato")
        pal = {}
        for cat, ids in [("herois", ["hero_01_knight", "hero_06_fire_mage"]), ("aldeoes", ["folk_01_blacksmith", "folk_15_noble_lady"]), ("monstros", ["monster_02_orc", "monster_14_ghost"])]:
            vals = []
            for id_ in ids:
                escolher(pg, cat, id_); vals.append((tema(pg), tok(pg, "--theme-primary"), tok(pg, "--theme-background"), tok(pg, "--theme-accent"), tok(pg, "--retrato-bg")))
            ck(f"tema {cat}: ambos os retratos ativam '{cat}'", all(v[0] == cat for v in vals), [v[0] for v in vals])
            ck(f"tema {cat}: retratos da mesma categoria compartilham EXATAMENTE a paleta", vals[0] == vals[1])
            pal[cat] = vals[0]
            pg.screenshot(path=f"/tmp/shots/tema_{cat}_app.png")
        ck("as 3 paletas sao distintas entre si", len({v[1] for v in pal.values()}) == 3 and len({v[2] for v in pal.values()}) == 3)
        escolher(pg, "monstros", "monster_02_orc")
        css = pg.evaluate("""()=>{const g=(s,p)=>getComputedStyle(document.querySelector(s))[p];return {menu:g('#menu','borderTopColor'),btn:g('.btn--primario','backgroundImage'),nivel:g('.perfil__nivel','color'),barra:g('.trilha--xp i','backgroundImage'),body:g('body','backgroundImage')}}""")
        ck("global: botao primario / nivel / body usam a paleta do tema (nao a original)", "163, 33, 43" not in css["btn"] and "140, 29, 46" in css["btn"] and "163, 33, 43" not in css["body"], css["btn"][:90])
        pg.reload(); pg.wait_for_selector("#retrato"); time.sleep(.4); ck("persiste apos recarregar", tema(pg) == "monstros")
        pg.click("#btnSair"); pg.wait_for_url("**/index.html"); ck("apos sair: login neutro (sem tema)", tema(pg) is None)
        pg.fill("#usuario", "Tester"); pg.fill("#senha", "senha-forte-1"); pg.click("#btnEntrar"); pg.wait_for_url("**/modulos.html"); pg.wait_for_selector("#retrato"); ck("tema restaurado apos novo login (vem do servidor)", tema(pg) == "monstros")
        pg.goto(BASE + "laboratorio.html"); pg.wait_for_selector("#retrato"); ck("tema aplicado tambem em outra pagina (laboratorio)", tema(pg) == "monstros")
        pg.goto(BASE + "modulos.html"); pg.wait_for_selector("#retrato")
        im = Image.open("frontend/assets/avatares/g/monster_02_orc.webp").convert("RGBA"); ck("WebP mantem alfa (cantos transparentes)", im.getpixel((2, 2))[3] == 0, im.getpixel((2, 2)))
        ck("retrato do perfil tem fundo tematico (nao #000)", pg.evaluate("getComputedStyle(document.querySelector('.perfil__retrato')).backgroundImage") != "none")
        pg.click("#btnAvatar"); pg.wait_for_selector("dialog.av[open]")
        arte = pg.evaluate("(()=>{const a=document.querySelector('.av__arte'),i=a.querySelector('img');return [getComputedStyle(a).backgroundImage!=='none', getComputedStyle(i).backgroundColor]})()")
        ck("modal: card tem fundo da categoria e <img> transparente", arte[0] and arte[1] in ("rgba(0, 0, 0, 0)", "transparent"), arte)
        ck("modal: atmosfera segue a aba navegada", pg.evaluate("document.querySelector('.av__caixa').dataset.tema") == "monstros")
        pg.click(".av__aba[data-cat=herois]"); ck("modal: mudar de aba muda a paleta do modal sem mudar o tema do app", pg.evaluate("document.querySelector('.av__caixa').dataset.tema") == "herois" and tema(pg) == "monstros")
        pg.screenshot(path="/tmp/shots/tema_modal_herois.png"); pg.click(".av__aba[data-cat=aldeoes]"); pg.screenshot(path="/tmp/shots/tema_modal_aldeoes.png"); pg.click(".av__aba[data-cat=monstros]"); pg.screenshot(path="/tmp/shots/tema_modal_monstros.png")
        pg.click("#avUpload"); pg.set_input_files("#avArq", {"name": "a.png", "mimeType": "image/png", "buffer": png()}); pg.wait_for_selector("#avCanvas:visible"); pg.click("#avSalvar"); pg.wait_for_selector("dialog.av", state="detached")
        ck("imagem personalizada remove o tema (neutro)", tema(pg) is None)
        ck("neutro = paleta original", tok(pg, "--theme-primary") == "#a3212b" and tok(pg, "--theme-background") == "#07080b")
        ck("sem aura/fundo tematico no retrato personalizado", pg.evaluate("getComputedStyle(document.querySelector('.perfil__retrato')).backgroundImage") == "none" and pg.evaluate("getComputedStyle(document.body).backgroundImage.includes('rgba(140')")==False)
        pg.reload(); pg.wait_for_selector("#retrato"); time.sleep(.4); ck("neutro persiste apos recarregar", tema(pg) is None)
        pg.click("#btnSair"); pg.wait_for_url("**/index.html"); pg.fill("#usuario", "Tester"); pg.fill("#senha", "senha-forte-1"); pg.click("#btnEntrar"); pg.wait_for_url("**/modulos.html"); pg.wait_for_selector("#retrato"); ck("neutro restaurado apos login", tema(pg) is None)
        pg.screenshot(path="/tmp/shots/tema_neutro_app.png")
        escolher(pg, "herois", "hero_04_ranger"); ck("trocar custom -> heroi reativa tema", tema(pg) == "herois")
        for cat in ["herois", "aldeoes", "monstros"]:
            pg.evaluate(f"document.documentElement.dataset.tema='{cat}'")
            v = {n: tok(pg, n) for n in ["--theme-surface", "--theme-primary", "--theme-muted", "--theme-raised", "--theme-accent-hi"]}
            c1 = contraste("#e8e4da", v["--theme-surface"]); c2 = contraste("#ffffff", v["--theme-primary"]); c3 = contraste(v["--theme-muted"], v["--theme-surface"]); c4 = contraste(v["--theme-accent-hi"], v["--theme-raised"])
            ck(f"contraste {cat}: texto {c1:.1f} | botao {c2:.1f} | muted {c3:.1f} | dourado {c4:.1f} (>=4.5)", min(c1, c2, c3, c4) >= 4.5)
        pg.evaluate("document.documentElement.dataset.tema='herois'")
        m = b.new_context(viewport={"width": 360, "height": 740}, device_scale_factor=2, is_mobile=True, has_touch=True); mp = m.new_page()
        mp.goto(BASE + "index.html"); mp.fill("#usuario", "Tester"); mp.fill("#senha", "senha-forte-1"); mp.click("#btnEntrar"); mp.wait_for_url("**/modulos.html"); mp.wait_for_selector("#retrato")
        mp.click("#btnAvatar"); mp.wait_for_selector("dialog.av[open]")
        for cat in ["herois", "aldeoes", "monstros"]:
            mp.click(f".av__aba[data-cat={cat}]"); time.sleep(.2)
            ov = mp.evaluate("(()=>{const d=document.querySelector('dialog.av'),g=document.querySelector('.av__grade');return d.scrollWidth<=d.clientWidth&&g.scrollWidth<=g.clientWidth&&document.documentElement.scrollWidth<=innerWidth})()")
            ck(f"mobile {cat}: modal sem overflow horizontal", ov)
        mp.screenshot(path="/tmp/shots/tema_modal_mobile.png")
        ck("sem erros de JS", not erros, erros); b.close()
finally: srv.terminate()
print(f"\n{sum(R)}/{len(R)} passaram"); sys.exit(0 if all(R) else 1)
