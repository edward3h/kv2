@param String id
@param String initialValue

<div class="grid grid-cols-[33%_1fr] grid-rows-[minmax(300px,_1fr)_repeat(2,_max-content)] gap-2 pt-2">
    <textarea class="w-full h-full textarea textarea-bordered font-mono" id="editor" data-roster-id="${id}">${initialValue}</textarea>
    <div class="w-full min-w-fit px-4 border-l border-l-base-content" id="viewer"></div>
    <div class="collapse collapse-arrow bg-info text-info-content w-full col-span-2">
        <input type="checkbox" />
        <div class="collapse-title font-bold min-h-0 p-1">
            <i class="fa-solid fa-question mr-1"></i>
            <span>Roster Help</span>
        </div>
        <div class="collapse-content">
            <p>Help goes here blah blah</p>
        </div>
    </div>
    <div class="collapse collapse-arrow bg-base-200 w-full col-span-2">
        <input type="checkbox" />
        <div class="collapse-title font-bold min-h-0 p-1">
            <i class="fa-solid fa-gear mr-1"></i>
            <span>Roster Settings</span>
        </div>
        <div class="collapse-content">
            <p>Public View</p>
        </div>
    </div>
</div>
