<template>
  <q-layout view="hHh Lpr fFf">
    <q-header elevated height-hint="64">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title>
          Simple Roster Editor
        </q-toolbar-title>

        <q-space />

        <div class="q-gutter-sm row items-center no-wrap">
          <q-btn v-if="userStore.loggedIn.value" round flat>
            <q-avatar size="26px">
              <img :src="userStore.avatar.value" :alt="userStore.name.value">
            </q-avatar>
            <q-menu auto-close>
              <q-list>
                <q-item clickable @click="doLogout">
                  <q-item-section>Logout</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
            <q-tooltip>Account</q-tooltip>
          </q-btn>
          <q-btn round v-else @click="doLogin"><q-icon name="login" /></q-btn>
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
    >
      <q-list>
        <q-item-label
          header
        >
          Links
        </q-item-label>

        <EssentialLink
          v-for="link in essentialLinks"
          :key="link.title"
          v-bind="link"
        />
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>

    <q-footer class="text-center">Simple Roster Editor by Ordo Acerbus</q-footer>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import EssentialLink from 'components/EssentialLink.vue';
import { useUserStore } from 'src/stores/user';
import { useQuasar } from 'quasar';
import LoginChooseProviderVue from 'src/components/LoginChooseProvider.vue';
import { useCookies } from '@vueuse/integrations/useCookies';

const linksList = [
  {
    title: 'Help',
    caption: 'Help',
    icon: 'help',
    link: '#'
  }
];

export default defineComponent({
  name: 'MainLayout',

  components: {
    EssentialLink
  },

  setup () {
    const leftDrawerOpen = ref(false)
    const userStore = useUserStore();
    const $q = useQuasar();
    return {
      essentialLinks: linksList,
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      },
      userStore,
      doLogin: () => $q.dialog({component: LoginChooseProviderVue}),
      doLogout: () => userStore.logout()
    }
  }
});
</script>
