<template>
<div class="editor">
  <codemirror
    v-model="rosterText"
    placeholder="Type your roster here."
    />
</div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { Codemirror } from 'vue-codemirror'

const props = defineProps<{modelValue: string}>()
const emit = defineEmits(['update:modelValue'])

const rosterText = computed({
  get() : string {
    return props.modelValue
  },

  set(v : string) {
    emit('update:modelValue', v)
  }
})
</script>
