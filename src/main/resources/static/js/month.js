function applyGalochkaState(cell, rawValue) {
    const value = Number.parseFloat(rawValue) || 0;
    const pageType = cell.dataset.pageType;

    cell.dataset.value = String(value);

    if (pageType === 'NUMBER') {
        const input = cell.querySelector('.numeric-value-input');
        if (input) input.value = value === 0 ? '' : String(value);
        return;
    }

    cell.replaceChildren();

    if (pageType === 'BINARY') {
        if (value !== 0) {
            const mark = document.createElement('span');
            mark.className = 'full-mark';
            mark.textContent = '✓';
            cell.appendChild(mark);
        }
        return;
    }

    const fullMarks = Math.floor(value);
    const hasHalf = value % 1 >= 0.5;

    for (let i = 0; i < fullMarks; i++) {
        const mark = document.createElement('span');
        mark.className = 'full-mark';
        mark.textContent = '✓';
        cell.appendChild(mark);
    }

    if (hasHalf) {
        const halfMark = document.createElement('span');
        halfMark.className = 'half-mark';
        cell.appendChild(halfMark);
    }
}

function formatValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? String(number) : '0';
}

function updateWeekSummaries(summaries) {
    const tbody = document.getElementById('activityRows');
    const norm = Number(tbody.dataset.weeklyNorm) || 0;

    summaries.forEach(summary => {
        summary.days.forEach(day => {
            const cell = document.querySelector(`[data-summary-date="${day.date}"]`);
            if (!cell) return;

            const total = cell.querySelector('.day-total');
            if (total) total.textContent = formatValue(day.total);
        });

        const weekBox = document.querySelector(`.week-total-box[data-week-start="${summary.weekStartDate}"]`);

        if (weekBox) {
            const effectiveTotal = Number(summary.weekTotal) + Number(summary.incomingOverhead);
            const showCompletedCheck = tbody.dataset.showWeekCompletedCheck === 'true';
            const showPercentage = tbody.dataset.showWeekPercentage === 'true';

            if (weekBox) {
                const effectiveTotal = Number(summary.weekTotal) + Number(summary.incomingOverhead);
                weekBox.textContent = formatValue(effectiveTotal);

                if (showCompletedCheck && norm > 0) {
                    updateWeekCompletedMark(weekBox, effectiveTotal >= norm);
                }

                if (showPercentage) {
                    updateWeekPercentage(summary.weekStartDate, summary.percentage);
                }
            }
        }

        updateOverhead(summary.weekStartDate, summary.incomingOverhead);
    });
}

function updateWeekPercentage(weekStartDate, percentage) {
    const element = document.querySelector(
        `[data-percentage-week-start="${weekStartDate}"]`
    );

    if (!element || percentage == null) return;

    element.textContent = `${percentage}%`;
}

function updateWeekCompletedMark(weekBox, completed) {
    let image = weekBox.parentElement.querySelector('.week-completed-mark');

    if (!completed) {
        if (image) image.remove();
        return;
    }

    if (image) return;

    image = document.createElement('img');
    image.className = 'week-completed-mark random-week-check';
    image.alt = 'Норма выполнена';
    image.dataset.checkKey = weekBox.dataset.checkKey;
    image.dataset.weekIndex = weekBox.dataset.weekIndex;
    image.dataset.monthKey = weekBox.dataset.monthKey;

    const variant = (Number(weekBox.dataset.weekIndex) % 3) + 1;
    const rotationKey = `week-check-rotation-v2-${image.dataset.checkKey}`;

    let rotation = localStorage.getItem(rotationKey);

    if (rotation === null) {
        rotation = Math.floor(Math.random() * 9) - 4;
        localStorage.setItem(rotationKey, String(rotation));
    }

    image.src = `/galochka-week-${variant}.png`;
    image.style.transform = `rotate(${rotation}deg)`;

    weekBox.insertAdjacentElement('afterend', image);
}

function updateOverhead(weekStartDate, value) {
    const cell = document.querySelector(`[data-overhead-week-start="${weekStartDate}"]`);
    if (!cell) return;

    let valueSpan = cell.querySelector('.overhead-value');
    let arrow = cell.querySelector('.overhead-arrow');
    const overhead = Number(value) || 0;

    if (overhead <= 0) {
        if (valueSpan) valueSpan.remove();
        if (arrow) arrow.remove();
        return;
    }

    if (!valueSpan) {
        valueSpan = document.createElement('span');
        valueSpan.className = 'overhead-value';
        cell.appendChild(valueSpan);
    }

    valueSpan.textContent = '+' + formatValue(overhead);

    if (!arrow && cell.previousElementSibling) {
        arrow = document.createElement('span');
        arrow.className = 'overhead-arrow';
        arrow.textContent = '→';
        cell.appendChild(arrow);
    }
}

document.addEventListener('click', function (event) {
    const cell = event.target.closest('.galochka-cell');
    if (!cell || cell.dataset.pageType === 'NUMBER') return;

    const tbody = document.getElementById('activityRows');
    const params = new URLSearchParams();

    params.append('activityId', cell.dataset.activityId);
    params.append('date', cell.dataset.date);
    params.append('year', tbody.dataset.year);
    params.append('month', tbody.dataset.month);

    fetch('/api/galochki/click', {method: 'POST', body: params})
        .then(response => {
            if (!response.ok) throw new Error('Ошибка сохранения галочки');
            return response.json();
        })
        .then(data => {
            applyGalochkaState(cell, data.value);
            updateWeekSummaries(data.weekSummaries);
        })
        .catch(error => {
            console.error(error);
            alert('Не удалось сохранить галочку');
        });
});

document.addEventListener('contextmenu', function (event) {
    const cell = event.target.closest('.galochka-cell');

    if (!cell) {
        return;
    }

    event.preventDefault();

    const activityId = cell.dataset.activityId;
    const date = cell.dataset.date;

    const params = new URLSearchParams();
    params.append('activityId', activityId);
    params.append('date', date);

    const tbody = document.getElementById('activityRows');
    params.append('year', tbody.dataset.year);
    params.append('month', tbody.dataset.month);

    fetch('/api/galochki/reset', {
        method: 'POST',
        body: params
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка сброса галочки');
            }

            return response.json();
        })
        .then(data => {
            applyGalochkaState(cell, data.value);
            updateWeekSummaries(data.weekSummaries);
        })
        .catch(error => {
            console.error(error);
            alert('Не удалось сбросить значение');
        });
});

document.addEventListener('change', function (event) {
    const input = event.target.closest('.numeric-value-input');

    if (!input) return;

    const cell = input.closest('.galochka-cell');
    const params = new URLSearchParams();

    params.append('activityId', cell.dataset.activityId);
    params.append('date', cell.dataset.date);

    if (input.value.trim() !== '') params.append('value', input.value);

    fetch('/api/galochki/value', {method: 'POST', body: params})
        .then(response => {
            if (!response.ok) throw new Error('Ошибка сохранения значения');
            return response.json();
        })
        .then(data => {
            applyGalochkaState(cell, data.value);
        })
        .catch(error => {
            console.error(error);
            alert('Не удалось сохранить значение');
        });
});

function togglePageEdit() {
    document.body.classList.toggle('page-edit-mode');
    document.body.classList.remove('page-create-mode');
}

function togglePageCreate() {
    document.body.classList.toggle('page-create-mode');
    document.body.classList.remove('page-edit-mode');
}

function toggleActivitiesEdit() {
    document.body.classList.toggle('activities-edit-mode');

    const tbody = document.getElementById('activityRows');
    if (tbody) updateDraggableRows(tbody);
}

document.addEventListener('submit', function (event) {
    const form = event.target.closest('.edit-title-form');

    if (!form) {
        return;
    }

    event.preventDefault();

    const input = form.querySelector('input[name="title"]');
    const activityId = form.querySelector('input[name="activityId"]').value;

    const params = new URLSearchParams();
    params.append('activityId', activityId);
    params.append('title', input.value);

    fetch('/activities/update', {
        method: 'POST',
        body: params
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка обновления');
            }

            const cell = form.closest('.activity-name');
            const viewTitle = cell.querySelector('.view-title');

            if (viewTitle) {
                viewTitle.textContent = input.value;
                input.dataset.original = input.value;
            }
        })
        .catch(error => {
            console.error(error);
            alert('Не удалось обновить');
        });
});

document.addEventListener('focusout', function (event) {
    const input = event.target.closest('.edit-title-form input[name="title"]');

    if (!input) {
        return;
    }

    if (input.value === input.dataset.original) {
        return;
    }

    input.form.requestSubmit();
});

document.addEventListener('dblclick', function (event) {
    if (!document.body.classList.contains('activities-edit-mode')) {
        return;
    }

    const groupName = event.target.closest('.group-name');

    if (!groupName) {
        return;
    }

    const groupRow = groupName.closest('.group-row');

    if (!groupRow || !groupRow.dataset.groupId) {
        return;
    }

    groupRow.classList.add('group-editing');

    const input = groupRow.querySelector('.group-title-form input[name="title"]');

    if (input) {
        input.focus();
        input.select();
    }
});

function refreshGroupSelectOptions() {
    const select = document.querySelector('select[name="groupId"]');

    if (!select) {
        return;
    }

    select.querySelectorAll('option[data-group-id]').forEach(option => option.remove());

    let index = 1;

    document.querySelectorAll('.group-row').forEach(groupRow => {
        const groupId = groupRow.dataset.groupId;

        if (!groupId) {
            return;
        }

        const title = groupRow.querySelector('.group-view-title').textContent.trim();
        const option = document.createElement('option');

        option.value = groupId;
        option.dataset.groupId = groupId;
        option.textContent = title || 'Группа ' + index;

        select.appendChild(option);
        index++;
    });
}

document.addEventListener('submit', function (event) {
    const form = event.target;

    if (
        form.matches('form[action="/activities"]') ||
        form.matches('form[action="/activity-groups"]') ||
        form.matches('form[action="/activities/delete"]') ||
        form.matches('form[action="/activity-groups/delete"]')
    ) {
        sessionStorage.setItem('activities-edit-mode', 'true');
    }
});

document.addEventListener('submit', function (event) {
    const form = event.target.closest('.group-title-form');

    if (!form) {
        return;
    }

    event.preventDefault();

    const groupRow = form.closest('.group-row');
    const input = form.querySelector('input[name="title"]');
    const groupId = form.querySelector('input[name="groupId"]').value;

    const params = new URLSearchParams();
    params.append('groupId', groupId);
    params.append('title', input.value);

    fetch('/activity-groups/update', {
        method: 'POST',
        body: params
    }).then(response => {
        if (!response.ok) {
            throw new Error('Ошибка обновления группы');
        }

        const viewTitle = groupRow.querySelector('.group-view-title');

        if (viewTitle) {
            viewTitle.textContent = input.value;
        }

        input.dataset.original = input.value;
        groupRow.classList.remove('group-editing');
        refreshGroupSelectOptions();
    }).catch(error => {
        console.error(error);
        alert('Не удалось обновить группу');
    });
});

document.addEventListener('focusout', function (event) {
    const input = event.target.closest('.group-title-form input[name="title"]');

    if (!input) {
        return;
    }

    const groupRow = input.closest('.group-row');

    if (input.value === input.dataset.original) {
        groupRow.classList.remove('group-editing');
        return;
    }

    input.form.requestSubmit();
});

let draggedRows = [];
let draggedType = null;
let orderChanged = false;

function updateWeeklyNormField(form) {
    const pageType = form.querySelector('.page-type-select');
    const normField = form.querySelector('.weekly-norm-field');
    const normInput = form.querySelector('input[name="weeklyNorm"]');
    const statsSetting = form.querySelector('.stats-without-norm-setting');
    const statsCheckbox = form.querySelector('input[name="showStatisticsWithoutNorm"]');
    const normSettings = form.querySelector('.norm-display-settings');

    if (!pageType || !normField || !normInput) return;

    const numeric = pageType.value === 'NUMBER';
    const hasNorm = Number(normInput.value) > 0;

    normField.style.display = numeric ? 'none' : '';
    normInput.disabled = numeric;

    if (statsSetting) {
        statsSetting.style.display = numeric ? 'none' : '';
    }

    if (statsCheckbox) {
        if (hasNorm) {
            statsCheckbox.checked = true;
            statsCheckbox.disabled = true;
        } else {
            statsCheckbox.disabled = false;
        }
    }

    if (normSettings) {
        normSettings.style.display = !numeric && hasNorm ? '' : 'none';
    }

    if (numeric) {
        normInput.value = '';
    }
}

function updateDraggableRows(tbody) {
    const editable = document.body.classList.contains('activities-edit-mode');

    tbody.querySelectorAll('.activity-row .activity-name').forEach(cell => {
        cell.draggable = editable;
    });

    tbody.querySelectorAll('.group-row .group-name').forEach(cell => {
        const groupRow = cell.closest('.group-row');

        cell.draggable =
            editable &&
            groupRow &&
            !!groupRow.dataset.groupId;
    });
}

function getGroupBlock(groupRow) {
    const rows = [groupRow];
    let next = groupRow.nextElementSibling;

    while (next && !next.classList.contains('group-row')) {
        rows.push(next);
        next = next.nextElementSibling;
    }

    return rows;
}

function insertRowsBefore(tbody, rows, targetRow) {
    rows.forEach(row => tbody.insertBefore(row, targetRow));
}

function insertRowsAfter(tbody, rows, targetRow) {
    const after = targetRow.nextElementSibling;

    rows.forEach(row => tbody.insertBefore(row, after));
}

function findGroupRowForActivity(activityRow) {
    let prev = activityRow.previousElementSibling;

    while (prev) {
        if (prev.classList.contains('group-row')) {
            return prev;
        }

        prev = prev.previousElementSibling;
    }

    return null;
}

function saveGroupOrder(tbody) {
    const pageId = tbody.dataset.pageId;

    const params = new URLSearchParams();
    params.append('pageId', pageId);

    tbody.querySelectorAll('.group-row[data-group-id]').forEach(row => {
        const groupId = row.dataset.groupId;

        if (groupId) {
            params.append('groupIds', groupId);
        }
    });

    fetch('/activity-groups/reorder', {
        method: 'POST',
        body: params
    }).then(response => {
        if (!response.ok) {
            throw new Error('Ошибка сохранения порядка групп');
        }
    }).catch(error => {
        console.error(error);
        alert('Не удалось сохранить порядок групп');
    });
}

function collectActivityGroups(tbody) {
    const result = [];

    tbody.querySelectorAll('.group-row').forEach(groupRow => {
        const rawGroupId = groupRow.dataset.groupId;
        const groupId = rawGroupId ? Number(rawGroupId) : null;

        const activityIds = [];
        let next = groupRow.nextElementSibling;

        while (next && !next.classList.contains('group-row')) {
            if (next.classList.contains('activity-row')) {
                activityIds.push(Number(next.dataset.activityId));
                next.dataset.groupId = rawGroupId || '';
            }

            next = next.nextElementSibling;
        }

        groupRow.classList.toggle('empty-group', activityIds.length === 0);

        result.push({
            groupId: groupId,
            activityIds: activityIds
        });
    });

    return result;
}

function saveActivityOrder(tbody) {
    const pageId = tbody.dataset.pageId;
    const payload = collectActivityGroups(tbody);

    fetch('/activities/reorder?pageId=' + encodeURIComponent(pageId), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    }).then(response => {
        if (!response.ok) {
            throw new Error('Ошибка сохранения порядка дел');
        }
    }).catch(error => {
        console.error(error);
        alert('Не удалось сохранить порядок дел');
    });
}

document.addEventListener('DOMContentLoaded', function () {
    if (sessionStorage.getItem('activities-edit-mode') === 'true') {
        document.body.classList.add('activities-edit-mode');
        sessionStorage.removeItem('activities-edit-mode');
    }

    document.querySelectorAll('.page-create-form').forEach(form => {
        updateWeeklyNormField(form);

        const typeSelect = form.querySelector('.page-type-select');
        const normInput = form.querySelector('input[name="weeklyNorm"]');

        if (typeSelect) {
            typeSelect.addEventListener('change', () => updateWeeklyNormField(form));
        }

        if (normInput) {
            normInput.addEventListener('input', () => updateWeeklyNormField(form));
        }
    });

    const tbody = document.getElementById('activityRows');

    if (!tbody) {
        return;
    }

    const weekChecks = document.querySelectorAll('.random-week-check');

    if (weekChecks.length > 0) {
        const monthKey = weekChecks[0].dataset.monthKey;
        const orderStorageKey = `week-check-order-v2-${monthKey}`;

        let variantsOrder;

        try {
            variantsOrder = JSON.parse(localStorage.getItem(orderStorageKey));
        } catch (error) {
            variantsOrder = null;
        }

        if (
            !Array.isArray(variantsOrder) ||
            variantsOrder.length !== 3
        ) {
            variantsOrder = [1, 2, 3];

            for (let i = variantsOrder.length - 1; i > 0; i--) {
                const j = Math.floor(Math.random() * (i + 1));

                [variantsOrder[i], variantsOrder[j]] =
                    [variantsOrder[j], variantsOrder[i]];
            }

            localStorage.setItem(
                orderStorageKey,
                JSON.stringify(variantsOrder)
            );
        }

        weekChecks.forEach(check => {
            const weekIndex = Number(check.dataset.weekIndex);
            const weekKey = check.dataset.checkKey;

            const rotationStorageKey =
                `week-check-rotation-v2-${weekKey}`;

            let rotation = localStorage.getItem(rotationStorageKey);

            if (rotation === null) {
                rotation = Math.floor(Math.random() * 9) - 4;

                localStorage.setItem(
                    rotationStorageKey,
                    String(rotation)
                );
            }

            const variant =
                variantsOrder[weekIndex % variantsOrder.length];

            check.src = `/galochka-week-${variant}.png`;
            check.style.transform = `rotate(${rotation}deg)`;
        });
    }
    
    tbody.querySelectorAll('.galochka-cell').forEach(cell => {
        applyGalochkaState(cell, cell.dataset.value);
    });

    updateDraggableRows(tbody);

    tbody.addEventListener('dragstart', function (event) {
        if (!document.body.classList.contains('activities-edit-mode')) {
            event.preventDefault();
            return;
        }

        if (event.target.closest('input, button, form')) {
            event.preventDefault();
            return;
        }

        const groupHandle = event.target.closest('.group-row .group-name');
        const activityHandle = event.target.closest('.activity-row .activity-name');

        if (groupHandle) {
            const groupRow = groupHandle.closest('.group-row');

            if (!groupRow.dataset.groupId) {
                event.preventDefault();
                return;
            }

            draggedType = 'group';
            draggedRows = getGroupBlock(groupRow);
        } else if (activityHandle) {
            draggedType = 'activity';
            draggedRows = [activityHandle.closest('.activity-row')];
        } else {
            event.preventDefault();
            return;
        }

        orderChanged = false;

        draggedRows.forEach(row => row.classList.add('dragging'));
    });

    tbody.addEventListener('dragend', function () {
        draggedRows.forEach(row => row.classList.remove('dragging'));

        if (orderChanged) {
            if (draggedType === 'group') {
                saveGroupOrder(tbody);
                saveActivityOrder(tbody);
            }

            if (draggedType === 'activity') {
                saveActivityOrder(tbody);
            }
        }

        draggedRows = [];
        draggedType = null;
        orderChanged = false;
    });

    tbody.addEventListener('dragover', function (event) {
        if (!draggedRows.length) {
            return;
        }

        event.preventDefault();

        const targetRow = event.target.closest('tr');

        if (!targetRow || draggedRows.includes(targetRow)) {
            return;
        }

        const rect = targetRow.getBoundingClientRect();
        const before = event.clientY < rect.top + rect.height / 2;

        if (draggedType === 'group') {
            const targetGroupRow = targetRow.classList.contains('group-row')
                ? targetRow
                : findGroupRowForActivity(targetRow);


            if (!targetGroupRow || draggedRows.includes(targetGroupRow)) {
                return;
            }

            if (!targetGroupRow.dataset.groupId) {
                return;
            }

            orderChanged = true;

            if (before) {
                insertRowsBefore(tbody, draggedRows, targetGroupRow);
            } else {
                const targetBlock = getGroupBlock(targetGroupRow);
                insertRowsAfter(tbody, draggedRows, targetBlock[targetBlock.length - 1]);
            }

            refreshGroupPlaceholders(tbody);
            return;
        }

        if (draggedType === 'activity') {
            orderChanged = true;

            if (targetRow.classList.contains('group-row')) {
                tbody.insertBefore(draggedRows[0], targetRow.nextElementSibling);
                return;
            }

            if (before) {
                tbody.insertBefore(draggedRows[0], targetRow);
            } else {
                tbody.insertBefore(draggedRows[0], targetRow.nextElementSibling);
            }
        }
    });
});

function refreshGroupPlaceholders(tbody) {
    let index = 1;

    tbody.querySelectorAll('.group-row').forEach(groupRow => {
        const placeholder = groupRow.querySelector('.group-edit-placeholder');

        if (!placeholder) {
            return;
        }

        if (!groupRow.dataset.groupId) {
            placeholder.textContent = 'Без группы';
            return;
        }

        placeholder.textContent = 'Группа ' + index;
        index++;
    });
}
