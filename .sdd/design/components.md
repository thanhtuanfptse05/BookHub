# components.md — Danh mục component

> **Luật dùng lại:** Trước khi tạo component mới, tra bảng dưới. Có rồi → dùng lại (hoặc thêm variant). Tạo mới xong → ghi thêm 1 dòng vào đây.
> Nguyên tắc chung: chỉ dùng token (`--color-*`, `--space-*`), prop theo hành vi (`variant`, `size`, `disabled`, `loading`), đủ trạng thái (`hover/focus-visible/disabled`), HTML ngữ nghĩa.

| Component | Biến thể (variant) | Trạng thái | Đường dẫn code | Dùng ở |
|---|---|---|---|---|
| Button | primary · secondary · ghost · danger · sm/md/lg | default · hover · focus · disabled · loading | {{...}} | |
| TextField | label · hint · error | default · focus · error · disabled | | |
| Select / Checkbox / Switch | | default · focus · checked · disabled · error | | |
| Card | flat · raised | default · interactive | | |
| Modal / Dialog | confirm · form | open · closed · loading | | |
| Toast / Alert | success · warning · danger · info | | | |
| Table / List | | loading(skeleton) · empty · error · data | | |
| EmptyState / ErrorState | | | | |
