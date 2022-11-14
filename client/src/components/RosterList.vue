<template>
  <div v-if="isFetching">Loading...</div>
  <q-list :class="$attrs.class">
    <q-item v-for="item in data" :key="item.id">
    <q-item-section><router-link :to="`/rosters/${item.id}`"><q-item-label>{{item.title}}</q-item-label></router-link></q-item-section>
    <q-item-section>
    <q-btn color="warning" icon="delete_outline" @click="deleteRoster(item.id)"/>
    </q-item-section>
    </q-item>
    <q-item><q-item-section><q-item-label>Create</q-item-label></q-item-section><q-item-section>
  <q-btn color="primary" icon="add_circle_outline" @click="newRoster"/>
  </q-item-section>
    </q-item>
  </q-list>
  <div>{{ error }}</div>
</template>
<script setup lang="ts">
import { useFetch } from '@vueuse/core'
import { useRouter } from 'vue-router';
const url = '/abc/rosters'
const { isFetching, error, data, execute: fetchList } = useFetch(url).get().json()
const router = useRouter();

const newRoster = async () => {
  const { data } = await useFetch(url).post(`# New Roster

  Type your roster entries here.`).json()
  console.log(data.value)
  router.push(`/rosters/${data.value.id}`)
}

const deleteRoster = (rosterId: string) => {
  useFetch(`/abc/rosters/${rosterId}`).delete().then(() => fetchList())
  return false;
}

</script>
