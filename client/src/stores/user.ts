import { useJwt } from '@vueuse/integrations/useJwt';
import { useCookies } from '@vueuse/integrations/useCookies';
import { computed } from 'vue';

interface NamedJwt {
  displayName?: string
  picture?: string
}

const cookies = useCookies(['JWT'])
const user = computed(() => {
  const jwtCookie = cookies.get('JWT')
  console.log(jwtCookie)
  if (jwtCookie) {
    const { payload } = useJwt<NamedJwt>(jwtCookie)
    if (payload) {
      const name = payload.value?.displayName
      const picture = payload.value?.picture
      if (name) {
        return { name, picture }
      }
    }
  }
  return {}
})
const userStore = {
  loggedIn: computed(() => user.value.name),
  name: computed(() => user.value.name),
  avatar: computed(() => user.value.picture || 'https://cdn.quasar.dev/img/boy-avatar.png'),
  logout: () => cookies.remove('JWT')
}
export const useUserStore = () => userStore
