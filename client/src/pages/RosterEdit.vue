<template>
<q-page>
  <template v-if="errorText">
    {{errorText}}
  </template>
  <template v-else-if="rosterText">
  <q-splitter v-model="split" separator-class="separator" separator-style="width: 5px">
    <template v-slot:before>
      <roster-editor v-model="rosterText"></roster-editor>
    </template>
    <template v-slot:after>
      <roster-view :rosterText="rosterText"></roster-view>
    </template>
  </q-splitter>
  </template>
  <template v-else>Loading...</template>
</q-page>
</template>
<script setup lang="ts">
import { useFetch, watchDebounced } from '@vueuse/core';
import { computed, ref } from 'vue';
import { onBeforeRouteUpdate, useRoute } from 'vue-router';
import RosterEditor from '../components/RosterEditor.vue'
import RosterView from '../components/RosterView.vue'

const split = ref(33)
const rosterText = ref()
const errorText = ref()

const loadRoster = async (rosterId: string) => {
  const { error, statusCode, data } = await useFetch(`/abc/rosters/${rosterId}`).get().json()
  if (error.value ) {
    console.log(error.value)
    if (statusCode.value === 401 || statusCode.value === 403) {
      errorText.value = 'You do not have permission to view this roster'
    }
    else {
      errorText.value = error.value
    }
  } else {
    console.log(data.value)
    rosterText.value = data.value.body
  }
}

const route = useRoute()
const rosterId = computed(() => route.params.id)
loadRoster(rosterId.value as string)

onBeforeRouteUpdate(async (to, from) => {
  if (to.params.id !== from.params.id) {
    loadRoster(to.params.id as string)
  }
})
watchDebounced(rosterText,
  () => {useFetch(`/abc/rosters/${rosterId.value}`).patch({
    body: rosterText.value
  })},
  {debounce: 500, maxWait: 1000}
)
</script>
<style>
.separator {
  border-left: 3px groove;
}
</style>
