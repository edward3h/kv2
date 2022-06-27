<template>
  <q-dialog ref="dialogRef" @hide="onDialogHide">
    <q-card class="q-dialog-plugin">
      <div class="text-h6">Login</div>
      <q-card-actions vertical>
        <q-btn @click="loginGoogle">Login with Google</q-btn>
        <q-btn @click="loginDiscord">Login with Discord</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { useDialogPluginComponent } from 'quasar';

defineEmits({
  ...useDialogPluginComponent.emits
})

const { dialogRef, onDialogHide, onDialogOK, onDialogCancel } = useDialogPluginComponent()

const loginGoogle = () => window.location.href = '/oauth/login/google'

const loginDiscord = () => window.location.href = '/oauth/login/discord'

</script>
