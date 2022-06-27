<template>
  <q-page class="row items-center justify-evenly">
    Hi {{ userStore.name.value }}
  </q-page>
</template>

<script setup lang="ts">
import { useUserStore } from 'src/stores/user';

const userStore = useUserStore();

</script>
