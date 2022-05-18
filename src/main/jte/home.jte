@import java.util.Optional
@import java.util.Map

@param boolean loggedIn
@param Optional<String> name
@param Map<String, Object> attributes

@template.main(
    title = "Home",
        content = @`
        <h1>Micronaut - OAuth example</h1>

<h2>username: <span>${name.orElse("Anonymous")}</span></h2>
<ul>
    @for (var entry : attributes.entrySet())
        @if ("picture".equals(entry.getKey()))
            <li><img src="${String.valueOf(entry.getValue())}"></li>
        @else
            <li>${entry.getKey()}: ${String.valueOf(entry.getValue())}</li>
        @endif
    @endfor
</ul>
<nav>
    <ul>
        @if (loggedIn)
            <li><a href="/logout">Logout</a></li>
    @else
        <li><a href="/oauth/login/google">Login with Google</a></li>
        <li><a href="/oauth/login/discord">Login with Discord</a></li>
    @endif
    </ul>
</nav>
`
)