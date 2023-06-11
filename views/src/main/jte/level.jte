@import org.ethelred.roster.Level

@param Level level

@if(level.getHeader() != null)
    !{var open = String.format("<h%d>", level.getHeader()); var close = String.format("</h%d>", level.getHeader());}
    $unsafe{open}${level.getText()}$unsafe{close}
@else
    @if(!level.isRoot())
    <div class="flex"><div class="${level.isAnnotation() ? "italic": ""} grow">${level.getText()}</div><div class="pl-4">${level.getTotal()}</div></div>
    @endif
    <div class="${level.isRoot() ? "" : "border-l pl-2"}">
        @for(var child: level.getChildren())
            @template.level(level = child)
        @endfor
    </div>
    @if(level.isRoot())
        <div class="font-bold flex"><div class="grow">${level.getText()}</div><div class="pl-4">${level.getTotal()}</div></div>
    @endif
@endif
