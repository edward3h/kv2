@import org.ethelred.roster.ParsedRoster

@param ParsedRoster roster

<style>
    @for(var style: roster.getStyles())
        ${style}
    @endfor
</style>
<div class="roster prose">
@template.level(level = roster.getRoot())
</div>