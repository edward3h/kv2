<template>
  <div :class="{'bg-teal-1': props.odd, 'bg-white': !props.odd }">
    <div class="details row no-wrap">
    <div :class="textClasses">{{level.text}}</div>
    <div class="total col-1" v-if="total">{{total}}</div>
    </div>
    <div class="q-pl-md">
      <roster-level v-for="(child, index) in level.children" :key="prefix + index" :level="child" :prefix="prefix + index + '_'" :odd="(index % 2 === 1) === odd"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Level } from './parser';

interface Props {
  level: Level
  prefix: string
  odd: boolean
}
const props = defineProps<Props>()
const textClasses = computed(() => {
  const o: Record<string, boolean> = { text: true, 'text-italic': Boolean(props.level.annotation), col: true}
  if (props.level.header) {
    let h = props.level.header
    if (h > 6) {
      h = 6
    }
    o[`text-h${h}`] = true
  }
  return o
})
const total = computed(() => {
  return props.level.total()
})
</script>
