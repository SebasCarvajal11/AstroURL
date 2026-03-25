(function () {
    const TOKEN_KEY = "astrourl_token";

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    function setMessage(text, isError) {
        const box = document.getElementById("messageBox");
        if (!box) return;
        box.textContent = text || "";
        box.style.color = isError ? "#ff6b9d" : "#5fffd6";
    }

    async function safeJson(response) {
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text || "Respuesta no valida" };
        }
    }

    function wireCommonUi() {
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) {
            logoutBtn.classList.toggle("hidden", !getToken());
            logoutBtn.addEventListener("click", () => {
                clearToken();
                window.location.href = "/";
            });
        }

        const loginLink = document.getElementById("loginLink");
        const registerLink = document.getElementById("registerLink");
        const hasToken = !!getToken();
        if (loginLink) loginLink.classList.toggle("hidden", hasToken);
        if (registerLink) registerLink.classList.toggle("hidden", hasToken);
    }

    function wireShortenForm() {
        const form = document.getElementById("shortenForm");
        if (!form) return;

        const resultCard = document.getElementById("resultCard");
        const shortUrlLink = document.getElementById("shortUrlLink");
        const copyBtn = document.getElementById("copyBtn");

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            setMessage("Generando enlace…", false);

            const url = document.getElementById("urlInput").value.trim();
            const headers = { "Content-Type": "application/json" };
            const token = getToken();
            if (token) headers.Authorization = `Bearer ${token}`;

            const response = await fetch("/api/v1/shorten", {
                method: "POST",
                headers,
                body: JSON.stringify({ url })
            });

            const data = await safeJson(response);
            if (!response.ok) {
                const detail =
                    data.message ||
                    [data.error, data.errorCode, data.title, data.detail].filter(Boolean).join(" · ") ||
                    "";
                setMessage(detail.trim() || `Error ${response.status} al acortar. Revisa la consola (F12).`, true);
                if (!detail.trim()) console.error("shorten failed", response.status, data);
                if (resultCard) resultCard.classList.add("hidden");
                return;
            }

            if (resultCard && shortUrlLink) {
                const code = data.code || "";
                if (code) {
                    shortUrlLink.href = `/${code}`;
                    shortUrlLink.textContent = `${window.location.origin}/${code}`;
                } else {
                    shortUrlLink.href = data.shortUrl || "#";
                    shortUrlLink.textContent = data.shortUrl || "";
                }
                resultCard.classList.remove("hidden");
            }
            setMessage("Listo: ya puedes copiar o abrir tu enlace corto.", false);
        });

        if (copyBtn && shortUrlLink) {
            copyBtn.addEventListener("click", async () => {
                await navigator.clipboard.writeText(shortUrlLink.textContent || "");
                setMessage("Copiado al portapapeles.", false);
            });
        }
    }

    function wireLogin() {
        const form = document.getElementById("loginForm");
        if (!form) return;

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            setMessage("Comprobando datos…", false);

            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            const response = await fetch("/api/v1/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await safeJson(response);
            if (!response.ok) {
                setMessage(data.message || "Correo o contraseña incorrectos.", true);
                return;
            }

            setToken(data.token);
            setMessage("Sesión iniciada. Redirigiendo…", false);
            setTimeout(() => { window.location.href = "/dashboard"; }, 700);
        });
    }

    function wireRegister() {
        const form = document.getElementById("registerForm");
        if (!form) return;

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            setMessage("Creando tu cuenta…", false);

            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            const response = await fetch("/api/v1/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await safeJson(response);
            if (!response.ok) {
                setMessage(data.message || "No se pudo registrar.", true);
                return;
            }

            setMessage("Cuenta creada. Ya puedes entrar con tu correo.", false);
            setTimeout(() => { window.location.href = "/login"; }, 900);
        });
    }

    async function wireDashboard() {
        const tbody = document.getElementById("urlsTableBody");
        if (!tbody) return;

        const token = getToken();
        if (!token) {
            window.location.href = "/login";
            return;
        }

        const response = await fetch("/api/v1/dashboard/urls", {
            headers: { Authorization: `Bearer ${token}` }
        });

        const data = await safeJson(response);
        if (!response.ok) {
            setMessage(data.message || "No se pudo cargar el dashboard.", true);
            if (response.status === 401 || response.status === 403) window.location.href = "/login";
            return;
        }

        if (!Array.isArray(data) || data.length === 0) {
            tbody.innerHTML = "<tr><td colspan='4'>Aún no tienes enlaces acortados. <a href='/'>Volver al inicio</a> para crear uno.</td></tr>";
            return;
        }

        tbody.replaceChildren();
        data.forEach((item) => {
            const tr = document.createElement("tr");
            const tdCode = document.createElement("td");
            tdCode.textContent = item.shortCode ?? "";
            const tdLink = document.createElement("td");
            const a = document.createElement("a");
            a.href = item.longUrl || "#";
            a.target = "_blank";
            a.rel = "noopener";
            a.textContent = item.longUrl ?? "";
            tdLink.appendChild(a);
            const tdClicks = document.createElement("td");
            tdClicks.textContent = String(item.clickCount ?? 0);
            const tdDel = document.createElement("td");
            const delBtn = document.createElement("button");
            delBtn.type = "button";
            delBtn.className = "btn-delete";
            delBtn.dataset.deleteId = String(item.id);
            delBtn.textContent = "Purgar";
            tdDel.appendChild(delBtn);
            tr.append(tdCode, tdLink, tdClicks, tdDel);
            tbody.appendChild(tr);
        });

        tbody.addEventListener("click", async (event) => {
            const btn = event.target.closest("[data-delete-id]");
            if (!btn) return;
            const id = btn.getAttribute("data-delete-id");

            const deleteResponse = await fetch(`/api/v1/dashboard/urls/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            });
            const deleteData = await safeJson(deleteResponse);
            if (!deleteResponse.ok) {
                setMessage(deleteData.message || "No se pudo eliminar.", true);
                return;
            }
            setMessage("Enlace eliminado.", false);
            btn.closest("tr").remove();
        });
    }

    wireCommonUi();
    wireShortenForm();
    wireLogin();
    wireRegister();
    wireDashboard();
})();
