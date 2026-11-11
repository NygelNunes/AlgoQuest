import asyncio, json
from playwright.async_api import async_playwright
BASE="http://127.0.0.1:5500/"
async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch()
        ctx = await b.new_context(viewport={"width":1440,"height":1000})
        await ctx.add_init_script("sessionStorage.setItem('aq_usuario', JSON.stringify({nomeUsuario:'mock',token:'t',pontuacao:120,xp:340,nivel:3}))")
        pg = await ctx.new_page(); erros=[]
        pg.on("pageerror", lambda e: erros.append("PAGEERR "+str(e)))
        pg.on("console", lambda m: erros.append("CONSOLE "+m.text) if m.type=="error" and "8080" not in m.text and "Failed to load resource" not in m.text else None)
        await pg.goto(BASE+"laboratorio.html"); await pg.wait_for_selector(".barra")
        await pg.screenshot(path="/tmp/s_bubble.png")
        n = await pg.evaluate("document.getElementById('linha').max")
        for _ in range(int(n)): await pg.click("#btnProx")
        print("bubble fim:", await pg.inner_text("#narra"))
        await pg.click("#btnManual"); 
        ids = await pg.evaluate("[...document.querySelectorAll('.barra')].map(b=>b.dataset.id)")
        await pg.click(".barra[data-id='0']"); await pg.click(".barra[data-id='1']")
        print("manual:", await pg.inner_text("#narra"))
        await pg.screenshot(path="/tmp/s_manual.png"); await pg.click("#btnManual")
        for sim in ["busca","hash","bst","avl"]:
            await pg.click(f"#aba-{sim}"); await pg.wait_for_timeout(300)
            n = int(await pg.evaluate("document.getElementById('linha').max"))
            await pg.fill("#linha", "0") if False else None
            for _ in range(n): await pg.click("#btnProx")
            await pg.wait_for_timeout(700)
            print(sim, "fim:", (await pg.inner_text("#narra"))[:110])
            await pg.screenshot(path=f"/tmp/s_{sim}.png")
        await pg.click("#aba-avl"); await pg.click("#btnDesafio")
        respostas=0; certas=0
        for _ in range(200):
            if await pg.is_visible("#pergunta .opcao"):
                opts = await pg.query_selector_all("#pergunta .opcao")
                await opts[0].click(); respostas+=1
                if await pg.query_selector("#pergunta .opcao.certa >> nth=0") and await pg.evaluate("document.querySelector('#pergunta .opcao.certa').dataset.n")=='0': certas+=1
                if respostas==6: await pg.screenshot(path="/tmp/s_desafio.png")
            if await pg.is_disabled("#btnProx"): break
            await pg.click("#btnProx")
        print("desafio: respondeu",respostas,"placar:",(await pg.inner_text("#placar"))[:160].replace("\n"," | "))
        await pg.fill("input[name=v]","1, 1"); await pg.click("button[type=submit]"); print("erro:", await pg.inner_text("#erroEntrada"))
        await pg.goto(BASE+"exercicio.html?modulo=5"); await pg.wait_for_selector(".opcao")
        print("painel pratica visivel:", await pg.is_visible("#btnPratica"))
        await pg.click("#btnPratica"); await pg.wait_for_selector("#praticaCorpo iframe"); await pg.wait_for_timeout(900)
        await pg.screenshot(path="/tmp/s_exercicio.png")
        await pg.goto(BASE+"modulos.html"); await pg.wait_for_selector(".convite"); await pg.screenshot(path="/tmp/s_modulos.png")
        await pg.set_viewport_size({"width":390,"height":844}); await pg.goto(BASE+"laboratorio.html?sim=hash"); await pg.wait_for_selector(".hl"); await pg.screenshot(path="/tmp/s_mobile.png")
        print("ERROS:", erros if erros else "nenhum")
        await b.close()
asyncio.run(main())
