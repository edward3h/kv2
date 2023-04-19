@import org.ethelred.kv2.viewmodels.RosterContext
@import org.ethelred.kv2.viewmodels.LayoutContext

@param RosterContext roster
@param LayoutContext layout

@template.layout(model = layout, content = @`
    <style>
        @for(var style: roster.parsed().getStyles())
            ${style}
        @endfor
    </style>
    <div class="roster prose">
    @template.level(level = roster.parsed().getRoot())
    </div>
`)