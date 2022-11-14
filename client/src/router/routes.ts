import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/IndexPage.vue') },
      { path: '/rosters/:id', component: () => import('pages/RosterEdit.vue')},
      { path: '/help', component: () => import('pages/HelpPage.vue')},
      { path: '/privacy', component: () => import('pages/privacy.md')},
      { path: '/terms', component: () => import('pages/terms.md')},

    ],
  },

  {
    path: '/oauth/login/:provider',
    component: () => import('pages/FakeLogin.vue')
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
