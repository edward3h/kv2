<template>
  <div v-if="isFetching">Loading...</div>
  <pre>{{ data }}</pre>
  <div>{{ error }}</div>
  <q-btn color="primary" label="New..." @click="newRoster"/>
</template>
<script setup lang="ts">
import { useFetch } from '@vueuse/core'
import { useRouter } from 'vue-router';
const url = '/abc/rosters'
const { isFetching, error, data } = useFetch(url)
const router = useRouter();

const newRoster = async () => {
  const { data } = await useFetch(url).post('').json()
  console.log(data.value)
  router.push(`/rosters/${data.value.id}`)
}

</script>
