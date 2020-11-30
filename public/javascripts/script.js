var notAuthorizedZones = [
    '/',
    '/login',
    '/register',
];

var globalOptions = {
  data: {
    menuActions: [
      {
        title: 'パスワード変更',
        to: '/passwd',
      },
      {
        title: 'ログアウト',
        action: function() {
          axios.get('/logout').then(function() {
              location.href = '/';
          });
        },
      },
      {
        title: '退会',
        to: '/deregister',
      },
    ],
  },
  computed: {
    authorized: function() {
      return !notAuthorizedZones.some(function (v) { return v.startsWith(location.pathname); });
    },
  },
};

axios.defaults.headers['csrf-token'] = authenticityToken;
axios.defaults.baseURL = '/api/';

if (window.options !== undefined) {
  options = deepmerge(options, globalOptions);
  options.el = '#app';
  options.vuetify = new Vuetify();
  var app = new Vue(options);
}

