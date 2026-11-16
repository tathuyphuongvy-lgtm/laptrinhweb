'use strict';
document.querySelectorAll('[data-confirm-delete]').forEach(form => form.addEventListener('submit', event => {
  if (!window.confirm('Bạn muốn xóa danh mục này? Thao tác này không thể hoàn tác.')) event.preventDefault();
}));
const form = document.getElementById('category-form');
if (form) {
  const name = form.elements.categoryname;
  const link = form.elements.images;
  const file = form.elements.images1;
  const image = document.getElementById('preview-image');
  const letter = document.getElementById('preview-letter');
  const remove = document.getElementById('remove-image');
  const hint = document.getElementById('upload-hint');
  const initial = image.dataset.initial;
  let objectUrl = null;
  function updateName() {
    const value = name.value.trim();
    document.getElementById('preview-name').textContent = value || 'Tên danh mục của bạn';
    document.getElementById('preview-initial').textContent = value.slice(0, 1).toUpperCase() || 'M';
    const label = value.toLocaleLowerCase('vi');
    const icon = /váy|đầm/.test(label) ? 'dress' : /quần/.test(label) ? 'trousers' : /khoác/.test(label) ? 'jacket' : /sơ mi/.test(label) ? 'shirt' : /phụ kiện|túi/.test(label) ? 'bag' : /giày|dép/.test(label) ? 'shoe' : 'tshirt';
    const garment = document.getElementById('preview-garment');
    garment.src = garment.dataset.base + icon + '.svg';
    document.getElementById('name-count').textContent = name.value.length + '/50';
  }
  function updateImage() {
    if (objectUrl) { URL.revokeObjectURL(objectUrl); objectUrl = null; }
    const selected = file.files[0];
    if (selected && selected.size > 2 * 1024 * 1024) {
      file.setCustomValidity('Ảnh tối đa 2 MB. Vui lòng chọn ảnh nhỏ hơn.');
      hint.textContent = 'Ảnh đã chọn vượt quá 2 MB.';
      return;
    }
    file.setCustomValidity('');
    let src = '';
    if (selected) { objectUrl = URL.createObjectURL(selected); src = objectUrl; }
    else if (!(remove && remove.checked)) {
      const url = link.value.trim();
      src = /^https?:\/\//i.test(url) ? url : (!url ? initial : '');
    }
    image.hidden = !src; letter.hidden = !!src;
    if (src) image.src = src;
    hint.textContent = selected ? 'Đã chọn: ' + selected.name : 'Ảnh tải lên được ưu tiên hơn link ảnh.';
  }
  image.addEventListener('error', () => { image.hidden = true; letter.hidden = false; });
  name.addEventListener('input', updateName);
  link.addEventListener('input', updateImage);
  file.addEventListener('change', updateImage);
  if (remove) remove.addEventListener('change', updateImage);
  form.querySelectorAll('[name="status"]').forEach(radio => radio.addEventListener('change', () => {
    const status = document.getElementById('preview-status');
    const on = form.elements.status.value === '1';
    status.className = 'badge ' + (on ? 'on' : 'off');
    status.querySelector('span').textContent = on ? 'Hoạt động' : 'Khóa';
  }));
  updateName();
}
