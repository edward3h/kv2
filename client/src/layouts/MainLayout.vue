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
          <q-btn v-if="userStore.loggedIn.value">
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
          <q-btn v-else @click="doLogin" icon-right="login" label="Login"></q-btn>
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
        <sidebar-link v-if="userStore.loggedIn.value" title="My Rosters" to="/" icon="list"/>
        <sidebar-link title="Help" icon="help" to="/help"/>
        <sidebar-link title="Privacy Policy" icon="privacy_tip" to="/privacy"/>
        <sidebar-link title="Terms & Conditions" icon="summarize" to="/terms"/>


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
import { useUserStore } from 'src/stores/user';
import { useQuasar } from 'quasar';
import LoginChooseProviderVue from 'src/components/LoginChooseProvider.vue';
import SidebarLink from 'components/SidebarLink.vue'
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'MainLayout',

  components: {
    SidebarLink
},

  setup () {
    const leftDrawerOpen = ref(false)
    const userStore = useUserStore();
    const $q = useQuasar();
    const router = useRouter();

    return {
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      },
      userStore,
      doLogin: () => $q.dialog({component: LoginChooseProviderVue}),
      doLogout: () => {
        userStore.logout()
        router.push('/')
      }
    }
  }
});
</script>
