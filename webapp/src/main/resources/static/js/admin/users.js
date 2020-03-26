let userTable;

$(document).ready(function () {
    userTable = requestUsersFromServer();

    // create administrator
    $("#create-user-button").button().on("click", createAdministrator);
    $("#cancel-create-user-button").button().on("click", resetCreateUserForm);
    $("#create-admin-user-form-close").button().on("click", resetCreateUserForm);

    // update user
    $("#update-user-button").button().on("click", updateUser);
    $("#cancel-update-user-button").button().on("click", resetUpdateUserForm);
    $(document).on("click", "#user-table tbody tr", showUpdateForm);
    $("#update-user-form-close").button().on("click", resetUpdateUserForm);
});

/**
 * Request users from REST endpoint via AJAX using DataTable jQuery Plugin.
 */
function requestUsersFromServer() {
    clearHtmlTable();
    return $('#user-table').DataTable({
        'ajax': {
            'url': '/rest/v1/users',
            'type': 'GET',
            'dataSrc': ''
        },
        'pagingType': 'simple_numbers',
        'columns': [
            {'data': 'publicId'},
            {'data': 'username'},
            {'data': 'email'},
            {'data': 'role'},
            {'data': 'enabled'},
            {'data': 'lastLogin'},
            {'data': 'createdDateTime'}
        ],
        "autoWidth": false, // fixes window resizing issue
        "columnDefs": [
            {
                "render": function (data) {
                    if (data === 'Administrator') {
                        return '<span class="badge badge-danger">' + data + '</span>';
                    }
                    else {
                        return '<span class="badge badge-info">' + data + '</span>';
                    }
                },
                "targets": 3
            },
            {
                "render": function (data) {
                    if (data) {
                        return '<span class="badge badge-success">Enabled</span>';
                    }
                    else {
                        return '<span class="badge badge-secondary">Disabled</span>';
                    }
                },
                "targets": 4
            },
            {
                "targets": [0],
                "visible": false
            }
        ]
    });
}

/**
 * Removes all tr elements from the table body.
 */
function clearHtmlTable() {
    $("#user-data tr").remove();
}

/**
 * Create a new administrator on the server.
 */
function createAdministrator () {
    $.post({
        url: '/rest/v1/users',
        data: createAdministratorCreateRequest(),
        type: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        success: onCreateAdministratorSuccess,
        error: function (errorResponse) {
            onCreateError(errorResponse, '#create-admin-user-validation-area')
        }
    });
}

/**
 * Creates the json payload from html form to create a new administrator.
 * @returns {string} Stringified json payload to create a new administrator.
 */
function createAdministratorCreateRequest() {
    return JSON.stringify({
        username: $("#username").val(),
        email: $("#email").val(),
        plainPassword: $("#plainPassword").val(),
        verifyPlainPassword: $("#verifyPlainPassword").val()
    });
}

/**
 * Success callback for creating a new administrator.
 * @param createResponse The json response
 */
function onCreateAdministratorSuccess(createResponse) {
    userTable.row.add(createResponse).draw(false);
    resetCreateUserForm();
    $('#create-admin-user-dialog').modal('hide');
}

/**
 * Error callback for creating a new administrator.
 * @param errorResponse     The json response
 * @param validationAreaId  ID of the area to display errors (create)
 */
function onCreateError(errorResponse, validationAreaId) {
    onError(errorResponse, validationAreaId);
}

/**
 * Error callback for updating a user.
 * @param errorResponse     The json response
 * @param validationAreaId  ID of the area to display errors (update)
 */
function onUpdateError(errorResponse, validationAreaId) {
    onError(errorResponse, validationAreaId);
}

/**
 * Error callback.
 * @param errorResponse     The json response
 * @param validationAreaId  ID of the area to display errors (create/update)
 */
function onError(errorResponse, validationAreaId) {
    resetValidationArea(validationAreaId);
    const validationMessageArea = $(validationAreaId);
    validationMessageArea.addClass("alert alert-danger");

    if (errorResponse.status === 400) { // BAD REQUEST
        validationMessageArea.append("The following errors occurred during server-side validation:");
        const errorsList = $('<ul>', {class: "errors mb-0"}).append(
          errorResponse.responseJSON.messages.map(message =>
            $("<li>").text(message)
          )
        );
        validationMessageArea.append(errorsList);
    }
    else if (errorResponse.status === 409) { // CONFLICT
        validationMessageArea.append(errorResponse.responseJSON.messages[0]);
    }
    else {
        validationMessageArea.append("An unexpected error has occurred. Please try again at a later time.");
    }
}

/**
 * Creates the json payload from html form to update a user.
 * @returns {string} Stringified json payload to update a user.
 */
function createUpdateUserRequest() {
    return JSON.stringify({
        publicUserId: $("#updatePublicId").val(),
        role: $("#updateRole").val(),
        enabled: $("#updateStatus").val() === "Enabled",
    });
}

/**
 * Shows the update form and fills form with values from the selected user.
 */
function showUpdateForm() {
    let data = userTable.row(this).data();
    $('#update-user-dialog').modal('show');

    // master data
    $('#updatePublicId').val(data.publicId);
    $('#updateUsername').val(data.username);
    $('#updateEmail').val(data.email);
    $('#updateRole').val(data.role);
    $('#updateStatus').val(data.enabled ? 'Enabled' : 'Disabled');

    // meta data
    $('#updateLastLogin').val(data.lastLogin);
    $('#updateCreatedBy').val(data.createdBy);
    $('#updateCreatedDateTime').val(data.createdDateTime);
    $('#updateLastModifiedBy').val(data.lastModifiedBy);
    $('#updateLastModifiedDateTime').val(data.lastModifiedDateTime);
}

/**
 * Updates a certain user.
 */
function updateUser () {
    sendUpdateUserRequest();
    resetUpdateUserForm();
    $('#update-user-dialog').modal('hide');
}

/**
 * Sends the update request to the server.
 */
function sendUpdateUserRequest() {
    $.post({
        url: '/rest/v1/users',
        data: createUpdateUserRequest(),
        type: 'PUT',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        success: onUpdateUserSuccess,
        error: function (errorResponse) {
            onUpdateError(errorResponse, '#update-user-validation-area')
        }
    });
}

/**
 * Success callback for updating a user.
 * @param updateResponse The json response
 */
function onUpdateUserSuccess(updateResponse) {
    userTable.rows().every(function (rowIndex) {
        if (userTable.cell(rowIndex, 1).data() === updateResponse.username) {
            userTable.cell(rowIndex, 3).data(updateResponse.role);
            userTable.cell(rowIndex, 4).data(updateResponse.enabled);
        }
    }).draw();

    resetUpdateUserForm();
    $('#update-user-dialog').modal('hide');
}

/**
 * Resets the user creation form.
 */
function resetCreateUserForm() {
    $("#create-admin-user-form")[0].reset();
    resetValidationArea('#create-admin-user-validation-area');
}

/**
 * Resets the user update form.
 */
function resetUpdateUserForm() {
    $("#update-user-form")[0].reset();
    resetValidationArea('#update-user-validation-area');
}

/**
 * Resets the validation area in create admin or update user form.
 * @param validationAreaId  ID of the area to reset (create/update)
 */
function resetValidationArea(validationAreaId) {
    const validationMessageArea = $(validationAreaId);
    validationMessageArea.removeClass('alert alert-danger');
    validationMessageArea.empty();
}