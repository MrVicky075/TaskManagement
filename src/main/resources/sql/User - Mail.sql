-- Mail templates for activity-based email notifications (SQL Server)
-- Safe to re-run: each insert is guarded by IF NOT EXISTS

    --------------------- User Register ---------------

INSERT INTO [dbo].[tbl_user_login]
(
    created_on,
    email,
    is_active,
    mobile_no,
    password,
    role,
    updated_on,
    user_name
)
VALUES
    (

    '2026-08-27 15:32:33.6480421',
    'vikas.songara@localmail.com',
    1,
    '9999999999',
    '$2a$12$SA/VwrFOMqHOt9HHgm50Eun5rvdL01DqlhpSUZYm6EfAs7v7ikcVa',
    'SR_DEVELOPER',
    '2026-09-07 15:58:15.1191765',
    'Vikas Songara'
    ),
    (
    '2026-08-27 15:41:44.8073022',
    'shubham@localmail.com',
    1,
    '9923456789',
    '$2a$12$5j12Lv6z6lRA/0bIezw7suYdNO/Jf7v7PfXC9h76zWPksxsXKfaBW',
    'DEVELOPER',
    '2026-08-27 15:41:44.8073022',
    'Shubham Tiwari'
    ),
    (
    '2026-08-31 11:19:20.4185684',
    'anis.mansuri@localmail.com',
    1,
    '9999999999',
    '$2a$12$906aKNPflTtzvxYO4BzDn.tQ/MJhqBcBt55n9xwa/VnHOrLeSH7qS',
    'DEVELOPER',
    '2026-09-08 11:34:32.6201389',
    'Anis Mansuri'
    ),
    (
    '2026-08-31 11:20:05.1686897',
    'harsh.siddhapura@localmail.com',
    1,
    '9999999999',
    '$2a$12$qA5X8ItGJSmKE/j7S7mVOOD6wRVejFUzVSQzWkQ8U9sR4/ZFeOpxm',
    'DEVELOPER',
    '2026-08-31 11:20:05.1686897',
    'Harsh Siddhapura'
    ),
    (
    '2026-08-31 11:20:56.3532243',
    'vimal.prajapati@localmail.com',
    1,
    '9999999999',
    '$2a$12$9S3pUXHyllsGSFE0MSaw0.oUOUVsfIEwPO2f/rIMu516lequiEYya',
    'DEVELOPER',
    '2026-08-31 11:20:56.3532243',
    'Vimal Prajapati'
    ),
    (
    '2026-08-31 11:21:27.3465791',
    'vikas.v@localmail.com',
    1,
    '9999999999',
    '$2a$12$u.cgIMsD6ohF2qUEwGQ1Hun5k6ITC2Cz5gX/482/ufU0ROOUxRD1u',
    'DEVELOPER',
    '2026-08-31 11:21:27.3465791',
    'Vikas Vishwakarma'
    ),
    (
    '2026-08-31 11:21:51.6207558',
    'akul.gehlot@localmail.com',
    1,
    '9999999999',
    '$2a$12$yIwXWYp2yE.QQ9.T6qnypeSE6JtLyZVhQtZOTkuqcSM5EQyhYiqWq',
    'DEVELOPER',
    '2026-08-31 11:21:51.6207558',
    'Akul Gehlot'
    ),
    (
    '2026-08-31 11:22:37.8495991',
    'darshan@localmail.com',
    1,
    '9999999999',
    '$2a$12$TIdKRwmXti/E8OhaD.DepO7sK/cufkg/hqffn6oUEelkiT4qGMc4G',
    'SR_DEVELOPER',
    '2026-08-31 11:22:37.8495991',
    'Darshan Shah'
    ),
    (
    '2026-08-31 11:23:05.3689049',
    'imran.s@localmail.com',
    1,
    '9999999999',
    '$2a$12$5CREyUpOSzknPFyvi5G/0.gCYM.tQFyny6bVsg3Q3L86qbfjBCJRi',
    'SUPPORT',
    '2026-08-31 11:23:05.3689049',
    'Imran Sodagar'
    ),
    (
    '2026-08-31 11:23:32.4445168',
    'samjadkhan@localmail.com',
    1,
    '9999999999',
    '$2a$12$SZVvEymUWS87KmuWhYd9O.m1AXXXrwzY1Xz4IgQA5ir/IFyTrDdrS',
    'SUPPORT',
    '2026-08-31 11:23:32.4445168',
    'Samjad Khan'
    ),
    (
    '2026-08-31 11:23:55.2122512',
    'vishal@localmail.com',
    1,
    '9999999999',
    '$2a$12$03APYCvrNs3B31X0qiwBXekpdmWrT9qIKY24RGWv8ceh40JJgw9BW',
    'SUPPORT',
    '2026-08-31 11:23:55.2122512',
    'Vishal Tiwari'
    ),
    (
    '2026-08-31 12:14:00.6487003',
    'temp@localmail.com',
    1,
    '9999999999',
    '$2a$12$YMoUqeKpUVoshv/7gRz2OOVhbT4h45OIG6hx.eUhp2Il/L8UQljzW',
    'DEVELOPER',
    '2026-08-31 12:14:00.6487003',
    '-'
    ),
    (
    '2026-08-31 12:35:01.2810559',
    'amrita.nayak@localmail.com',
    1,
    '9999999999',
    '$2a$12$wgEQYbBGes91nsExS7bWBuWIeNYMSCXvfnhh5ROnWIl9LJNULKyfi',
    'DEVELOPER',
    '2026-08-31 12:35:01.2810559',
    'Amrita Nayak'
    );


-- --------------------- Mail Template ---------------------
-- USER_REGISTERED
IF NOT EXISTS (SELECT 1 FROM tbl_mail_template WHERE template_code = 'USER_REGISTERED')
BEGIN
    INSERT INTO tbl_mail_template (template_code, subject, body, is_active, created_at, updated_at)
    VALUES (
        'USER_REGISTERED',
        'PT Task Mgmt - Welcome $userName',
        N'<html><body>
            <h2>Welcome $userName</h2>
            <p>Your account has been successfully registered.</p>
            <p>Email: <b>$email</b></p>
            <p>User ID: <b>$userId</b></p>
            <p>Thank you for registering with us.</p>
        </body></html>',
        1, GETDATE(), GETDATE()
    );
END
GO

-- USER_UPDATED
IF NOT EXISTS (SELECT 1 FROM tbl_mail_template WHERE template_code = 'USER_UPDATED')
BEGIN
    INSERT INTO tbl_mail_template (template_code, subject, body, is_active, created_at, updated_at)
    VALUES (
        'USER_UPDATED',
        'PT Task Mgmt - User Profile Updated - $userName',
        N'<html><body>
            <h2>User Profile Updated</h2>
            <p>Hello $userName,</p>
            <p>Your user profile has been updated.</p>
            <p>User ID: <b>$userId</b><br/>Email: <b>$email</b></p>
            <p><b>Changes:</b></p>
            $changes
            <p>Thank you.</p>
        </body></html>',
        1, GETDATE(), GETDATE()
    );
END
GO

-- TASK_CREATED
IF NOT EXISTS (SELECT 1 FROM tbl_mail_template WHERE template_code = 'TASK_CREATED')
BEGIN
    INSERT INTO tbl_mail_template (template_code, subject, body, is_active, created_at, updated_at)
    VALUES (
        'TASK_CREATED',
        'PT Task Mgmt - New Task Assigned - $taskId',
        N'<html><body>
            <h2>Task Assigned</h2>
            <p>Hello $assignedUser,</p>
            <p>A new task has been assigned to you.</p>
            <p>
                <b>Task ID:</b> $taskId<br/>
                <b>Task:</b> $taskName<br/>
                <b>Priority:</b> $priority<br/>
                <b>Status:</b> $status<br/>
                <b>Client:</b> $clientName<br/>
                <b>Issue:</b> $issue
            </p>
            <p><b>Task Details:</b><br/>$taskDetails</p>
            <p>Please review the task.</p>
            <p>Thank you.</p>
        </body></html>',
        1, GETDATE(), GETDATE()
    );
END
GO

-- TASK_UPDATED
IF NOT EXISTS (SELECT 1 FROM tbl_mail_template WHERE template_code = 'TASK_UPDATED')
BEGIN
    INSERT INTO tbl_mail_template (template_code, subject, body, is_active, created_at, updated_at)
    VALUES (
        'TASK_UPDATED',
        'PT Task Mgmt - Task Updated - $taskId',
        N'<html><body>
            <h2>Task Updated</h2>
            <p>Hello $assignedUser,</p>
            <p>The following task has been updated.</p>
            <p>
                <b>Task ID:</b> $taskId<br/>
                <b>Task:</b> $taskName
            </p>
            <p><b>Changes:</b></p>
            $changes
            <p>Please review the updated task.</p>
            <p>Thank you.</p>
        </body></html>',
        1, GETDATE(), GETDATE()
    );
END
GO

-- TASK_DELETED
IF NOT EXISTS (SELECT 1 FROM tbl_mail_template WHERE template_code = 'TASK_DELETED')
BEGIN
    INSERT INTO tbl_mail_template (template_code, subject, body, is_active, created_at, updated_at)
    VALUES (
        'TASK_DELETED',
        'PT Task Mgmt - Task Deleted - $taskId',
        N'<html><body>
            <h2>Task Deleted</h2>
            <p>Hello $assignedUser,</p>
            <p>The following task has been deleted.</p>
            <p>
                <b>Task ID:</b> $taskId<br/>
                <b>Task:</b> $taskName<br/>
                <b>Priority:</b> $priority<br/>
                <b>Status:</b> $status<br/>
                <b>Client:</b> $clientName<br/>
                <b>Assigned To:</b> $assignedUser
            </p>
            <p>This task is no longer available.</p>
            <p>Thank you.</p>
        </body></html>',
        1, GETDATE(), GETDATE()
    );
END
GO
