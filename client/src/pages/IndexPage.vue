<template>
  <q-page class="row items-center justify-evenly">
    <roster-list class="col" v-if="userStore.loggedIn.value"/>
    <welcome class="col" v-else/>
  </q-page>
</template>

<script setup lang="ts">
import { useUserStore } from 'src/stores/user';
import Welcome from 'src/components/WelcomeComponent.vue';
import RosterList from '../components/RosterList.vue';

const userStore = useUserStore();

</script>
