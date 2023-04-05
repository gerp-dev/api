# Per richieste di tipo GET:
# api.get('?manage=ricerca&sub=findUsers')

# Per richieste di tipo POST:
# api.post('?manage=ricerca&sub=findUsers', {'term': 'gerp'})

class GerpApi:
    def __init__(self, username, password, host):
        self.username = username
        self.password = password
        self.host = host.rstrip('/') + '/'

    def get(self, url):
        print(self.request('get', url))
        exit()

    def post(self, url, options={}):
        print(self.request('post', url, options))
        exit()

    def request(self, type, url, options={}):
        type = type.lower()

        if type != 'get' and type != 'post':
            raise Exception('Il tipo di richiesta deve essere o "get" o "post", "' + type + '" fornito.')

        params = {}
        url_components = urlparse(url)

        if 'query' not in url_components:
            raise Exception('I parametri nell\'URL devono iniziare con il punto di domanda ("?"). Es.: "?manage=ricerca".')

        parse_qs(url_components.query, params)

        if 'view' not in params or params['view'] != 'json':
            params['view'] = 'json'

        url = self.host + 'index.php?' + urlencode(params)

        data = {
            'auth_username': self.username,
            'auth_secret': self.password
        }

        if options:
            data.update(options)

        curl = pycurl.Curl()
        curl.setopt(pycurl.URL, url)
        curl.setopt(pycurl.TIMEOUT, 30)
        curl.setopt(pycurl.POST, 1)
        curl.setopt(pycurl.POSTFIELDS, urlencode(data))
        curl.setopt(pycurl.WRITEFUNCTION, lambda x: None)

        try:
            curl.perform()
            response = curl.getvalue().decode('utf-8')
            curl.close()
            return response
        except pycurl.error:
            raise Exception('Errore nella comunicazione con "' + url + '".')
