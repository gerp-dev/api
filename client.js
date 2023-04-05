// For GET requests:
// api.get('?manage=ricerca&sub=findUsers');
// 
// For POST requests:
// api.post('?manage=ricerca&sub=findUsers', {term: 'gerp'});

class GerpApi {
  constructor(username, password, host) {
    this.username = username;
    this.password = password;
    this.host = host.endsWith('/') ? host.slice(0, -1) : host;
  }

  request(type, url, options = {}) {
    type = type.toLowerCase();

    if (type !== 'get' && type !== 'post') {
      throw new Error(`Il tipo di richiesta deve essere o "get" o "post", "${type}" fornito.`);
    }

    const params = {};
    const url_components = new URL(url);

    if (!url_components.search) {
      throw new Error('I parametri nell\'URL devono iniziare con il punto di domanda ("?"). Es.: "?manage=ricerca".');
    }

    url_components.searchParams.forEach((value, key) => {
      params[key] = value;
    });

    if (!params.view || params.view !== 'json') {
      params.view = 'json';
    }

    url = `${this.host}/index.php?${new URLSearchParams(params)}`;

    const data = {
      auth_username: this.username,
      auth_secret: this.password,
      ...options
    };

    return fetch(url, {
      method: type.toUpperCase(),
      body: type === 'post' ? JSON.stringify(data) : undefined,
      headers: {
        'Content-Type': 'application/json'
      }
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`Errore nella comunicazione con "${url}".`);
        }
        return response.json();
      });
  }

  get(url) {
    return this.request('get', url);
  }

  post(url, options) {
    return this.request('post', url, options);
  }
}

const api = new GerpApi('', '', '');
