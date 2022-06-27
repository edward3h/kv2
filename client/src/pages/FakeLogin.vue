<template>
<div>Fake login page for dev. {{ route.params.provider }}</div>
<div><q-btn @click="doLogin">Login</q-btn></div>
</template>

<script setup lang="ts">
import { useCookies } from '@vueuse/integrations/useCookies';
import { useRoute } from 'vue-router';

const route = useRoute()
const doLogin = () => {
  const cookies = useCookies()
  cookies.set('JWT', 'eyJhbGciOiJub25lIn0.eyJzdWIiOiIyQXhJcXR5RVZDUUk1OE5MMjFVTjFDTm9KODkiLCJuYmYiOjE2NTU5NDUwMDMsImRpc3BsYXlOYW1lIjoiRWR3YXJkIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlzcyI6Imt2MiIsImZsYWdzIjpbIlJPTEVfVVNFUiJdLCJleHAiOjE2NTU5NDg2MDMsImlhdCI6MTY1NTk0NTAwMywicGljdHVyZSI6Imh0dHBzOlwvXC9jZG4uZGlzY29yZGFwcC5jb21cL2F2YXRhcnNcLzI4NzY1ODUyNjkwNDU0OTM4OFwvNTIyYTBmZTI5N2Y0Y2M3M2MyNmU4YjgzZmViYmMzODEucG5nP3NpemU9MTI4In0.',
    {path: '/'})
  window.location.href = '/'
}
</script>

