const state = {
    currentUserId: null
};

const output = document.querySelector("#output");
const currentUserId = document.querySelector("#currentUserId");

function formData(form) {
    return Object.fromEntries(new FormData(form).entries());
}

function setCurrentUser(user) {
    state.currentUserId = user.id;
    currentUserId.textContent = user.id;
}

function showResult(data) {
    output.textContent = JSON.stringify(data, null, 2);
}

function showError(error) {
    showResult({
        error: error.message
    });
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (response.status === 204) {
        return null;
    }

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Request failed");
    }

    return data;
}

function parcelPayload(form) {
    const data = formData(form);

    return {
        name: data.name,
        location: data.location,
        size: Number(data.size),
        cropType: data.cropType,
        lastIrrigation: data.lastIrrigation || null,
        notes: data.notes
    };
}

function schedulePayload(form) {
    const data = formData(form);

    return {
        intervalDays: Number(data.intervalDays),
        waterAmount: Number(data.waterAmount),
        active: form.elements.active.checked
    };
}

function renderParcels(parcels) {
    const container = document.querySelector("#parcelsList");

    container.innerHTML = parcels.map(parcel => `
        <article class="card">
            <strong>${parcel.name}</strong>
            <div class="meta">
                ID: ${parcel.id}<br>
                Location: ${parcel.location}<br>
                Size: ${parcel.size}<br>
                Crop: ${parcel.cropType}<br>
                Last irrigation: ${parcel.lastIrrigation || "-"}<br>
                Notes: ${parcel.notes || "-"}
            </div>
        </article>
    `).join("");
}

function renderHistory(items) {
    const container = document.querySelector("#historyList");

    container.innerHTML = items.map(item => `
        <article class="list-item">
            <strong>History ID: ${item.id}</strong>
            <div class="meta">
                Parcel ID: ${item.parcelId}<br>
                Date: ${item.irrigationDate}<br>
                Time: ${item.irrigationTime}<br>
                Water: ${item.waterAmount}
            </div>
        </article>
    `).join("");
}

function renderSchedules(items) {
    const container = document.querySelector("#schedulesList");

    container.innerHTML = items.map(item => `
        <article class="list-item">
            <strong>Schedule ID: ${item.id}</strong>
            <div class="meta">
                Parcel ID: ${item.parcelId}<br>
                Interval: ${item.intervalDays} days<br>
                Water: ${item.waterAmount}<br>
                Active: ${item.active}
            </div>
        </article>
    `).join("");
}

document.querySelector("#registerForm").addEventListener("submit", async event => {
    event.preventDefault();

    try {
        const data = formData(event.currentTarget);
        const user = await api("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(data)
        });
        setCurrentUser(user);
        showResult(user);
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#loginForm").addEventListener("submit", async event => {
    event.preventDefault();

    try {
        const data = formData(event.currentTarget);
        const user = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(data)
        });
        setCurrentUser(user);
        showResult(user);
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#findUserForm").addEventListener("submit", async event => {
    event.preventDefault();

    try {
        const data = formData(event.currentTarget);
        const user = await api(`/api/users/${data.userId}`);
        setCurrentUser(user);
        showResult(user);
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#parcelForm").addEventListener("submit", async event => {
    event.preventDefault();

    if (!state.currentUserId) {
        showError(new Error("Login, register, or find a user first."));
        return;
    }

    try {
        const parcel = await api(`/api/parcels/user/${state.currentUserId}`, {
            method: "POST",
            body: JSON.stringify(parcelPayload(event.currentTarget))
        });
        showResult(parcel);
        await loadParcels();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#updateParcelBtn").addEventListener("click", async () => {
    const form = document.querySelector("#parcelForm");
    const data = formData(form);

    if (!data.id) {
        showError(new Error("Enter parcel ID before updating."));
        return;
    }

    try {
        const parcel = await api(`/api/parcels/${data.id}`, {
            method: "PUT",
            body: JSON.stringify(parcelPayload(form))
        });
        showResult(parcel);
        await loadParcels();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#deleteParcelBtn").addEventListener("click", async () => {
    const data = formData(document.querySelector("#parcelForm"));

    if (!data.id) {
        showError(new Error("Enter parcel ID before deleting."));
        return;
    }

    try {
        await api(`/api/parcels/${data.id}`, {
            method: "DELETE"
        });
        showResult({ deletedParcelId: Number(data.id) });
        await loadParcels();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#loadParcelsBtn").addEventListener("click", loadParcels);

async function loadParcels() {
    if (!state.currentUserId) {
        showError(new Error("Login, register, or find a user first."));
        return;
    }

    try {
        const parcels = await api(`/api/parcels/user/${state.currentUserId}`);
        renderParcels(parcels);
        showResult(parcels);
    } catch (error) {
        showError(error);
    }
}

document.querySelector("#historyForm").addEventListener("submit", async event => {
    event.preventDefault();

    const data = formData(event.currentTarget);

    try {
        const history = await api(`/api/irrigation-history/parcel/${data.parcelId}`, {
            method: "POST",
            body: JSON.stringify({
                waterAmount: Number(data.waterAmount)
            })
        });
        showResult(history);
        await loadHistory();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#loadHistoryBtn").addEventListener("click", loadHistory);

async function loadHistory() {
    const data = formData(document.querySelector("#historyForm"));

    if (!data.parcelId) {
        showError(new Error("Enter parcel ID before loading history."));
        return;
    }

    try {
        const items = await api(`/api/irrigation-history/parcel/${data.parcelId}`);
        renderHistory(items);
        showResult(items);
    } catch (error) {
        showError(error);
    }
}

document.querySelector("#scheduleForm").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.currentTarget;
    const data = formData(form);

    try {
        const schedule = await api(`/api/schedules/parcel/${data.parcelId}`, {
            method: "POST",
            body: JSON.stringify(schedulePayload(form))
        });
        showResult(schedule);
        await loadSchedules();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#updateScheduleBtn").addEventListener("click", async () => {
    const form = document.querySelector("#scheduleForm");
    const data = formData(form);

    if (!data.id) {
        showError(new Error("Enter schedule ID before updating."));
        return;
    }

    try {
        const schedule = await api(`/api/schedules/${data.id}`, {
            method: "PUT",
            body: JSON.stringify(schedulePayload(form))
        });
        showResult(schedule);
        await loadSchedules();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#executeScheduleBtn").addEventListener("click", async () => {
    const data = formData(document.querySelector("#scheduleForm"));

    if (!data.id) {
        showError(new Error("Enter schedule ID before executing."));
        return;
    }

    try {
        await api(`/api/schedules/${data.id}/execute`, {
            method: "POST"
        });
        showResult({ executedScheduleId: Number(data.id) });
        await loadHistory();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#deleteScheduleBtn").addEventListener("click", async () => {
    const data = formData(document.querySelector("#scheduleForm"));

    if (!data.id) {
        showError(new Error("Enter schedule ID before deleting."));
        return;
    }

    try {
        await api(`/api/schedules/${data.id}`, {
            method: "DELETE"
        });
        showResult({ deletedScheduleId: Number(data.id) });
        await loadSchedules();
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#loadSchedulesBtn").addEventListener("click", loadSchedules);

async function loadSchedules() {
    const data = formData(document.querySelector("#scheduleForm"));

    if (!data.parcelId) {
        showError(new Error("Enter parcel ID before loading schedules."));
        return;
    }

    try {
        const items = await api(`/api/schedules/parcel/${data.parcelId}`);
        renderSchedules(items);
        showResult(items);
    } catch (error) {
        showError(error);
    }
}

document.querySelector("#recommendationForm").addEventListener("submit", async event => {
    event.preventDefault();

    const data = formData(event.currentTarget);

    try {
        const recommendation = await api(`/api/recommendations/parcel/${data.parcelId}/daily`);
        showResult(recommendation);
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#calculateForm").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.currentTarget;
    const data = formData(form);

    try {
        const result = await api("/api/recommendations/calculate", {
            method: "POST",
            body: JSON.stringify({
                parcelId: Number(data.parcelId),
                temperature: Number(data.temperature),
                humidity: Number(data.humidity),
                rainExpected: form.elements.rainExpected.checked
            })
        });
        showResult(result);
    } catch (error) {
        showError(error);
    }
});

document.querySelector("#clearOutputBtn").addEventListener("click", () => {
    showResult({});
});
