@import org.ethelred.kv2.viewmodels.StubView
@import java.util.List

@param boolean loggedIn
@param List<? extends StubView> rosters

<h1>Hi!</h1>
@if (loggedIn)
        <h2>Your Rosters</h2>
        <table>
                @for(var stub: rosters)
                <tr>
                        <td><a href="/roster/${stub.id()}">${stub.title()}</a></td>
                        <td><a href="/roster/${stub.id()}/edit" class="btn">Edit</a></td>
                </tr>
                @endfor
                <tr>
                        <td class="italic">New Roster</td>
                        <td><form method="post" action="/roster/new">
                                        <input type="submit" class="btn" value="Create">
                                </form></td>
                </tr>
        </table>
@else
        <p>Log in to start creating rosters.</p>
        <div class="text-center"><label for="login-modal" class="btn text-xl normal-case">Login</label></div>
@endif