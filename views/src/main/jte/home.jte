@import org.ethelred.kv2.viewmodels.HomeContext
@import org.ethelred.kv2.viewmodels.LayoutContext

@param HomeContext context
@param LayoutContext layoutContext

@template.layout(model = layoutContext,
        content = @`
        <h1>Hi!</h1>
        @if (layoutContext.loggedIn())
                <h2>Your Rosters</h2>
                <table>
                        @for(var stub: context.rosters())
                                <tr><td><a href="/roster/${stub.id()}">${stub.title()}</a></td>
                                        <td><a href="/roster/${stub.id()}/edit" class="btn">Edit</a></td></tr>
                        @endfor
                        <tr><td class="italic">New Roster</td><td><a href="/roster" class="btn">Create</a></td></tr>
                </table>
        @else
                <p>Log in to start creating rosters.</p>
                <div class="text-center"><label for="login-modal" class="btn text-xl normal-case">Login</label></div>
        @endif
`
)