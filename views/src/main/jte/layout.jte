@import gg.jte.Content
@import org.ethelred.kv2.viewmodels.Assets
@import org.ethelred.kv2.viewmodels.LayoutContext

@param Assets assets 
@param LayoutContext    model
@param Content content

@if (model.isFragment())
    ${content}
@else

<html lang="en">

<head>
    <title>${model.title()}</title>
    @for(var stylePath: assets.styles())
        <link href="/assets/${stylePath}.css" rel="stylesheet" type="text/css" />
    @endfor
    <link rel="apple-touch-icon" sizes="180x180" href="/assets/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="/assets/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="/assets/favicon-16x16.png">
<%--    <script src="https://kit.fontawesome.com/d96215f501.js" crossorigin="anonymous"></script>--%>
    @for(var scriptPath: assets.scripts())
        <script src="/assets/${scriptPath}.js"></script>
    @endfor
</head>

<body onload="main()">
    <nav class="navbar bg-primary text-primary-content">
        <div class="navbar-start">
            <div class="tooltip tooltip-bottom" data-tip="Menu (doesn't do anything yet)">
                <button class="btn btn-square btn-ghost">
                    <i class="fa-solid fa-bars text-xl"></i>
                </button>
            </div>
            <div class="tooltip tooltip-bottom" data-tip="Home">
                <a href="/" class="btn btn-square btn-ghost ${"Home".equals(model.title()) ? "btn-disabled" : ""}">
                    <i class="fa-solid fa-house text-xl"></i>
                </a>
            </div>
        </div>
        <div class="navbar-center">
            <div class="btn btn-ghost text-xl normal-case">${model.title()}</div>
        </div>
        <div class="navbar-end">
            @if(model.loggedIn())
                <div class="dropdown dropdown-end">
                <label tabindex="0" class="cursor-pointer">
                @if (model.pictureUrl() == null)
                    <div class="avatar placeholder">
                        <div class="bg-neutral-focus text-neutral-content rounded w-12">
                            <span>${model.placeholder()}</span>
                        </div>
                    </div>
                @else
                    <div class="avatar">
                      <div class="w-12 rounded">
                        <img src="${model.pictureUrl()}" alt="${model.username()} Avatar" />
                      </div>
                    </div>
                @endif
                </label>
                    <ul tabindex="0" class="menu dropdown-content text-black border-black">
                        <li><a href="/logout">Logout</a></li>
                        <li>TODO</li>
                    </ul>
                </div>
            @else
                <label for="login-modal" class="btn btn-ghost text-xl normal-case">Login</label>
            @endif
        </div>
    </nav>
    <div id="content" class="grow-0 min-w-fit m-auto p-2">
    ${content}
    </div>
    <footer class="footer p-2 bg-warning text-warning-content my-2">
        <div>
            <div><i class="fa-solid fa-triangle-exclamation mr-1"></i><span>Alpha mode: data may not be backed up. Features may change without warning.</span> </div>
        </div>
    </footer>
    <input type="checkbox" id="login-modal" class="modal-toggle" />
<div class="modal">
  <div class="modal-box">
    <h3 class="font-bold text-lg">Login</h3>
      <div>
      @for(var provider: model.providers())
          <a class="btn" href="${provider.path()}">${provider.display()}</a>
      @endfor
      </div>
      <div class="bg-warning text-warning-content p-2 my-2"><i class="fa-solid fa-triangle-exclamation mr-1"></i><span>Alpha mode: only pre-authorised users can log in.</span> </div>
      <div class="modal-action">
      <label for="login-modal" class="btn">Close</label>
    </div>
  </div>
</div>
</body>

</html>
@endif
