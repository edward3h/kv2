@import gg.jte.Content
@import org.ethelred.kv2.viewmodels.AuthContext
@import org.ethelred.kv2.viewmodels.PageContext

@param PageContext page
@param AuthContext auth
@param Content content

<html>

<head>
    <title>${page.title()}</title>
    <!-- TODO use build for minimizing css -->
    <link href="https://cdn.jsdelivr.net/npm/daisyui@2.51.4/dist/full.css" rel="stylesheet" type="text/css" />
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://kit.fontawesome.com/d96215f501.js" crossorigin="anonymous"></script>
</head>

<body>
    <nav class="navbar bg-primary text-primary-content">
        <div class="navbar-start">
            <button class="btn btn-square btn-ghost">
                <i class="fa-solid fa-bars text-xl"></i>
            </button>
        </div>
        <div class="navbar-center">
            <div class="btn btn-ghost text-xl normal-case">${page.title()}</div>
        </div>
        <div class="navbar-end">
            @if(auth.loggedIn())
            @else
                <label for="login-modal" class="btn btn-ghost text-xl normal-case">Login</label>
            @endif
        </div>
    </nav>
    <div id="content">
    ${content}
    </div>
    <input type="checkbox" id="login-modal" class="modal-toggle" />
<div class="modal">
  <div class="modal-box">
    <h3 class="font-bold text-lg">Login</h3>
      @for(var provider: auth.providers())
          <a class="btn" href="${provider.path()}">${provider.display()}</a>
      @endfor
    <div class="modal-action">
      <label for="login-modal" class="btn">Close</label>
    </div>
  </div>
</div>
</body>

</html>