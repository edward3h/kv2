<template>
  <div v-html="styleBlock"></div>
  <roster-level :level="roster.root" prefix=""></roster-level>
  <div>Total: {{roster.root.total()}}</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import parse from './parser';
import RosterLevel from './RosterLevel.vue'

interface Props {
  rosterText: string
}

const props = defineProps<Props>()
const roster = computed(() => {
  return parse(props.rosterText)
})
const styleBlock = computed(() => {
  return `<style>${roster.value.styles.join('\n')}</style>`
})
</script>
