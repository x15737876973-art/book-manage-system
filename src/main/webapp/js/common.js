/**
 * 图书管理系统 - 通用JavaScript
 */

// 显示模态框
function showModal(title, content) {
    // 创建遮罩层
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'modalOverlay';
    
    // 创建模态框
    overlay.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="modal-close" onclick="closeModal()">&times;</button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
        </div>
    `;
    
    document.body.appendChild(overlay);
    
    // 点击遮罩层关闭
    overlay.addEventListener('click', function(e) {
        if (e.target === overlay) {
            closeModal();
        }
    });
    
    // ESC键关闭
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
        }
    });
}

// 关闭模态框
function closeModal() {
    const overlay = document.getElementById('modalOverlay');
    if (overlay) {
        overlay.remove();
    }
}

// 确认删除
function confirmDelete(message, callback) {
    const content = `
        <div class="msg msg-warning">
            <strong>⚠️ 警告：</strong>${message}
        </div>
        <div style="text-align: center; margin-top: 20px;">
            <button class="btn btn-secondary" onclick="closeModal()">取消</button>
            <button class="btn btn-danger" id="confirmBtn">确认删除</button>
        </div>
    `;
    
    showModal('确认删除', content);
    
    document.getElementById('confirmBtn').addEventListener('click', function() {
        closeModal();
        if (callback) callback();
    });
}

// 显示成功消息
function showSuccess(message) {
    showMessage(message, 'success');
}

// 显示错误消息
function showError(message) {
    showMessage(message, 'error');
}

// 显示消息
function showMessage(message, type = 'info') {
    const msgDiv = document.createElement('div');
    msgDiv.className = `msg msg-${type}`;
    msgDiv.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        z-index: 3000;
        min-width: 300px;
        animation: slideInRight 0.3s;
    `;
    msgDiv.innerHTML = message;
    
    document.body.appendChild(msgDiv);
    
    // 3秒后自动消失
    setTimeout(() => {
        msgDiv.style.animation = 'slideOutRight 0.3s';
        setTimeout(() => {
            msgDiv.remove();
        }, 300);
    }, 3000);
}

// 添加动画样式
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// 表单验证
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    
    const inputs = form.querySelectorAll('[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            isValid = false;
            input.style.borderColor = '#dc3545';
            
            // 添加错误提示
            let errorSpan = input.nextElementSibling;
            if (!errorSpan || !errorSpan.classList.contains('error-text')) {
                errorSpan = document.createElement('span');
                errorSpan.className = 'error-text';
                errorSpan.style.cssText = 'color: #dc3545; font-size: 12px; margin-top: 5px; display: block;';
                input.parentNode.insertBefore(errorSpan, input.nextSibling);
            }
            errorSpan.textContent = '此项为必填项';
        } else {
            input.style.borderColor = '#ddd';
            const errorSpan = input.nextElementSibling;
            if (errorSpan && errorSpan.classList.contains('error-text')) {
                errorSpan.remove();
            }
        }
    });
    
    return isValid;
}

// 格式化日期
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 文字省略提示
function enableEllipsisTooltip() {
    const elements = document.querySelectorAll('.text-ellipsis');
    elements.forEach(el => {
        if (el.scrollWidth > el.clientWidth) {
            el.title = el.textContent;
        }
    });
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    enableEllipsisTooltip();
});