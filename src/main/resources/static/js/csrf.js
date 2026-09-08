(function () {
  const originalFetch = window.fetch;

  function readCookie(name) {
    const prefix = name + "=";
    const cookie = document.cookie.split("; ").find(value => value.startsWith(prefix));
    return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : null;
  }

  window.fetch = function (input, init) {
    const options = init ? { ...init } : {};
    const method = (options.method || "GET").toUpperCase();
    if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
      const headers = new Headers(options.headers || {});
      const token = readCookie("XSRF-TOKEN");
      if (token && !headers.has("X-XSRF-TOKEN")) headers.set("X-XSRF-TOKEN", token);
      options.headers = headers;
    }
    return originalFetch(input, options);
  };
})();
