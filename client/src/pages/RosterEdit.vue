<template>
<q-page>
  <q-splitter v-model="split" separator-class="separator" separator-style="width: 5px">
    <template v-slot:before>
      <roster-editor v-model="rosterText"></roster-editor>
    </template>
    <template v-slot:after>
      <roster-view :rosterText="rosterText"></roster-view>
    </template>
  </q-splitter>
</q-page>
</template>
<script setup lang="ts">
import { useFetch, watchDebounced } from '@vueuse/core';
import { computed, ref } from 'vue';
import { onBeforeRouteUpdate, useRoute } from 'vue-router';
import RosterEditor from '../components/RosterEditor.vue'
import RosterView from '../components/RosterView.vue'

const split = ref(33)
const rosterText = ref('')
const route = useRoute()
const rosterId = computed(() => route.params.id)
onBeforeRouteUpdate(async (to, from) => {
  if (to.params.id !== from.params.id) {
    const { data } = await useFetch(`/abc/rosters/${to.params.id}`).get().json()
    rosterText.value = data.value.body
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
